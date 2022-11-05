
<template>
  <div id="app">
    <div class="tit">对话语料构建系统</div>
    <div>当事人：{{username}}  <el-button type="warning" icon="el-icon-switch-button" size="mini" circle @click="logout"></el-button></div>
    <div class="img">
      <Graph :graphData="graphData" style="width: 50%" @changeShowedProperty="changeShowedProperty"></Graph>

      <div style="width: 50%; height: 100%;">
        <div style="text-align: center;width: 100%; height: 12%;">
          {{showedProperty['name']}}({{showedProperty['label']}})
        </div>
        <div style="width: 100%; height: 88%; overflow-y: auto">
          <el-collapse  accordion>
            <el-collapse-item  v-for="item,index in showedProperty" v-if="index!='name' && index!='label'" :key="index">
            <span class="collapse-title" slot="title">
              {{index}}
            </span>
              <div >
                {{item}}
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>

    </div>
    <div class="dia">
      <div class="group" v-for="item,index in list" :key="index">
        <div v-for="it,i in 2" :key="i">
          <div class="cont" :class="i==0?'right':'left'">
            <el-input :class="i==0?'ques':'ans'" v-model="item[i].sentence" :placeholder="i==0?'Question'+(index+1):'Answer'+(index+1)" @input="handleSubmit(index)">
            </el-input>

            <div class="bot">
              <div style="display: flex;align-items: center;overflow-x: auto;width: 85%;">
                <el-tag :key="tag" v-for="tag in item[i].entity" closable :disable-transitions="false" size="small"
                        @close="handleClose(index,i,tag)">
                  {{tag}}
                </el-tag>
              </div>

              <el-input class="input-new-tag" v-if="item[i].inputVisible" v-model="item[i].inputValue"
                        :ref="`saveTagInput${i}`" size="mini" @keyup.enter.native="handleInputConfirm(index,i)"
                        @blur="handleInputConfirm(index,i)">
              </el-input>
              <el-button v-else class="button-new-tag" style="margin-left: 10px;" size="mini"
                         @click="showInput(index,i)">
                添加</el-button>

              <el-select v-model="item[i].intention" placeholder="Relation" size="small" style="margin-left: 5px;">
                <el-option v-for="it in options" :key="it.value" :label="it.label" :value="it.value">
                </el-option>
              </el-select>
            </div>
          </div>
        </div>
      </div>
      <el-button @click="delBtn(list.length-1)" v-if="list.length>1" class="delBtn" size="mini" type="danger" icon="el-icon-delete" circle></el-button>
    </div>
    <div class="line"></div>
    <div class="btn psr">
      <el-button @click="killEntity" type="danger">毙掉</el-button>
      <el-button @click="clearAllInput" type="warning">清空</el-button>
      <el-button @click="changeBtn" type="info">换一个</el-button>
      <el-button :disabled="canbeSubmit" @click="prepareForm" type="success">保存</el-button>
      <div v-if="list[list.length-1][0].sentence.length>0" @click="addGroup" class="el-icon-circle-plus-outline psa"></div>
    </div>

    <el-dialog title="对话详情" :visible.sync="show" width="50%" :before-close="handleClose">
      <div style="max-height: 500px; overflow-y: auto;padding-right: 10px;">
        <table v-for="item,index in list" :key="index">
          <tr>
            <td>对话轮次</td>
            <td>{{index+1}}</td>
          </tr>
          <tr>
            <td>用户问题</td>
            <td>{{item[0].sentence}}</td>
          </tr>
          <tr>
            <td>问题答案</td>
            <td>{{item[1].sentence}}</td>
          </tr>
          <tr>
            <td>用户意图</td>
            <td>{{ item[0].intention }}</td>
          </tr>
          <tr>
            <td>主题实体</td>
            <td>{{ item[0].entity }}</td>
          </tr>
          <tr>
            <td>答案实体</td>
            <td>{{ item[1].entity }}</td>
          </tr>
          <tr>
            <td>实体子图</td>
            <td>{{ {"subGraph":graphData.edges,"properties":[topicEntityInfo]} }}</td>
          </tr>
        </table>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="show = false">取 消</el-button>
        <el-button type="primary" @click="submitBtn">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Graph from "@/components/Graph";
import request from "@/utils/request";
export default {
  name: "CorpusCollection",
  data() {
    return {
      topicEntityInfo : {},
      entityInfo :{},
      show: false,
      canbeSubmit: true,
      graphData:{},
      subGraph:[],
      showedProperty: {},
      username: "",
      // options: [{
      //   label:" 1 位于", value: "位于"},
      //   {label:" 2 生产", value: "生产"},
      //   {label:' 3 防治', value: '防治'},
      //   {label:" 4 病发于", value: '病发于'},
      //   {label:" 5 详细地址", value: '详细地址'},
      //   {label:" 6 联系人", value: '联系人'},
      //   {label:" 7 毒性", value: '毒性'},
      //   {label:" 8 剂型", value: '剂型'},
      //   {label:" 9 存储方式", value: '存储方式'},
      //   {label:"10 技术要点", value: '技术要点'},
      //   {label: "11 施用方法", value: '施用方法'},
      //   {label: "12 注意事项", value: '注意事项'},
      //   {label: "13 组成成分", value: '组成成分'},
      //   {label: "14 保质期", value: '保质期'},
      //   {label: "15 急救方法", value: '急救方法'},
      //   {label: "16 产品性能", value: '产品性能'}],
      options: [],
      list: [[{
        sentence: '',
        entity: [],
        intention:'',
        inputVisible: false,
        inputValue: '',
      }, {
        sentence: '',
        entity: [],
        intention: '',
        inputVisible: false,
        inputValue: '',
      }]]
    }
  },
  created() {
    this.username = localStorage.getItem('username')
    this.initPage()
  },
  methods:{
    prepareForm(){
      this.show = true;
    },
    handleSubmit(index){
      if (this.list[index][0].sentence.length>0)
        this.canbeSubmit = false;
      else
        this.canbeSubmit = true;
    },
    handleClose(index, num, tag) {
      this.list[index][num].entity.splice(this.list[index][num].entity.indexOf(tag), 1);
    },
    showInput(i, num) {
      this.list[i][num].inputVisible = true;
      this.$nextTick(_ => {
        this.$refs[`saveTagInput${num}`].$refs.input.focus();
      });
    },

    handleInputConfirm(index, num) {
      let inputValue = this.list[index][num].inputValue;
      if (inputValue) {
        this.list[index][num].entity.push(inputValue);
      }
      this.list[index][num].inputVisible = false;
      this.list[index][num].inputValue = '';
    },
    addGroup(){
      this.canbeSubmit = true;
      this.list.push([{
        sentence: '',
        entity: [],
        intention:'',
        inputVisible: false,
        inputValue: '',
    }, {
        sentence: '',
        entity: [],
        intention: '',
        inputVisible: false,
        inputValue: '',
      }])
    },
    delBtn(i){
      if (this.list[i-1][0]['sentence'].length>0)
        this.canbeSubmit = false
      else
        this.canbeSubmit = true

      console.log(this.canbeSubmit)
      this.list.splice(i,1)
    },
    changeBtn(){
      request.post("/api/collect-dialog-corpus", {'username':this.username, 'id':this.graphData.id}).then(res=>{
        this.graphData = res.data
        this.entityInfo = {}
        request.post("/api/collect-dialog-corpus/queryInfo", {"entityName":this.graphData.entity, "pdno":this.graphData.pdno}).then(res=>{
          this.showedProperty = res.data;
          this.topicEntityInfo = this.showedProperty;
          this.entityInfo[this.graphData.entity] = res.data
        })
      })
    },
    logout(){console.log(this.graphData)
      request.post("/api/logout", {'username':this.username, 'id':this.graphData.id}).then(res=>{
        localStorage.removeItem('username')
        this.$router.push('/')
      })
    },
    entityinfoBtn(){
      request.post("/api/collect-dialog-corpus/queryInfo",'花生配方肥').then(res=>{
        console.log(res)
      })
    },
    submitBtn(){
      request.post("/api/collect-dialog-corpus/saveRecords",{'dialog':this.list,'triples':this.graphData.edges, 'username':this.username, "properties":this.showedProperty}).then(res=>{
        this.initPage();
        this.canbeSubmit = true
        this.show = false
      })
    },
    killEntity(){
      request.post("/api/collect-dialog-corpus/killEntity", {'username':this.username, 'id':this.graphData.id}).then(res=>{
        this.graphData = res.data
        this.entityInfo = {}
        request.post("/api/collect-dialog-corpus/queryInfo", {"entityName": this.graphData.entity}).then(res=>{
          this.entityInfo[this.graphData.entity] = res.data
          this.showedProperty = res.data;
        })
      })
    },
    changeShowedProperty(newValue) {
      if (this.entityInfo[newValue])
        this.showedProperty = this.entityInfo[newValue]
      else {
        request.post("/api/collect-dialog-corpus/queryInfo", {"entityName": newValue}).then(res => {
          this.showedProperty = res.data
        })
      }
    },
    clearAllInput(){
      this.canbeSubmit=true,
      this.list = [[{
        sentence: '',
        entity: [],
        intention:'',
        inputVisible: false,
        inputValue: '',
      }, {
        sentence: '',
        entity: [],
        intention:'',
        inputVisible: false,
        inputValue: '',
      }]]
    },
    initPage(){
      let uri = "/api/collect-dialog-corpus/";
      request.get(uri+this.username).then(res => {
        this.initEntityInfo(res)
      });

      request.get(uri+"intentions").then(res=>{
        this.options = res.data

        console.log(this.options)
      });
    },

    initEntityInfo(res){
      this.graphData = res.data;
      this.list = [[{
        sentence: '',
        entity: [],
        intention:'',
        inputVisible: false,
        inputValue: '',
        properties: {}
      }, {
        sentence: '',
        entity: [],
        intention:'',
        inputVisible: false,
        inputValue: '',
      }]]
      request.post("/api/collect-dialog-corpus/queryInfo", {'entityName':this.graphData.entity, 'pdno':this.graphData.pdno}).then(res=>{
        this.showedProperty = res.data;
        this.topicEntityInfo = this.showedProperty;
        this.entityInfo[this.graphData.entity] = res.data
      })
    }
  },
  props:{
  },
  components:{
    Graph,
  }
}
</script>

<style lang="scss">
* {
  padding: 0;
  margin: 0;
  list-style: none;
}

::-webkit-scrollbar {
  /*滚动条整体样式*/
  width: 4px;
  /*高宽分别对应横竖滚动条的尺寸*/
  height: 1px;
}

::-webkit-scrollbar-thumb {
  /*滚动条里面小方块*/
  border-radius: 10px;
  box-shadow: inset 0 0 5px rgba(97, 184, 179, 0.1);
  background: #ccc;
}

::-webkit-scrollbar-track {
  /*滚动条里面轨道*/
  box-shadow: inset 0 0 5px rgba(87, 175, 187, 0.1);
  border-radius: 10px;
  background: #eaeaea;
}


.el-tag+.el-tag {
  margin-left: 10px;
}

.button-new-tag {
  // margin-left: 10px;
  // height: 32px;
  // line-height: 30px;
  // padding-top: 0;
  // padding-bottom: 0;
}

.input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}

#app {
  max-width: 750px;
  max-height: 100vh;
  background-color: #efefef;
  border-radius: 5px;
  margin: 0 auto;
  padding: 0 30px 50px;
  box-sizing: border-box;

  .tit {
    text-align: center;
    font-size: 20px;
    line-height: 40px;
    color: #333;
  }

  .img {
    width: 100%;
    height: 200px;
    margin: 0 auto;
    border: 1px solid #ccc;
    border-radius: 10px;
    box-sizing: border-box;
    display: flex;
  }

  .dia {
    padding-bottom: 20px;
    max-height: 400px;
    overflow-y: auto;

    .group {
      width: 100%;
      padding: 10px 0;
      position: relative;

      .delBtn {
        position: absolute;
        bottom: 50px;
        right: 20px;
      }

      .cont {
        width: 60%;
        padding: 5px 0 0;

        .ques .el-input__inner {
          background-color: #ffa0a0;
          color: #fff;
          outline: none;
        }

        .ans .el-input__inner {
          background-color: #b4d1ec;
        }

        .bot {
          width: 100%;
          display: flex;
          justify-content: end;
          padding-top: 5px;
        }
      }

      .right {
        padding-left: 40%;
      }
    }
  }

  .line {
    width: 100%;
    height: 2px;
    background-color: #ccc;
  }

  .btn {
    display: flex;
    justify-content: center;
    padding-top: 20px;
  }

  .psr {
    position: relative;

    .psa {
      position: absolute;
      top: 10px;
      right: 10px;
    }
  }
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;

  tr {

    td {
      line-height: 40px;
      text-align: center;

      &:nth-of-type(1) {
        width: 20%;
      }
    }
  }
}

table,
tr,
td {
  border: 1px solid #999;
}


.ques>input::-webkit-input-placeholder,
.ans>input::-webkit-input-placeholder {
  /* 使用webkit内核的浏览器 */
  color: #fff !important;
}

.collapse-title {
  flex: 1 0 90%;
  order: 1;
}
.el-collapse-item__header {
  flex: 1 0 auto;
  order: -1;
}
.el-collapse-item__arrow{
  padding-left: 10px;
}

</style>
