<template>
  <div id="network" class="vis-network"></div>
</template>


<style lang="scss">
#network {
  padding: 0;
  margin: 0;
  //list-style: none;
  width: 50%;
  height: 100%;
}
</style>

<script>
import Vis from "vis"
import request from "@/utils/request";

export default {
  name: "Graph",
  props:{
    graphData: {},
  },
  data() {
    return {
      givenEntityInfo:{},
      dialogVisable: false,
      nodes: [],
      edges: [],
      container: null,
      nodesArray: [
        // {id: 10, label: "STC"},
        // {id: 1, label: "IIM"},
      ],
      edgesArray: [
        // {from: 10, to: 1, label: "corperation"},
      ],
      options: {},
      data: {},
      graphDatav:{}
    };
  },
  methods:{
    init() {
      let _this = this;
      //1.创建一个nodes数组
      _this.nodes = new Vis.DataSet(_this.nodesArray);
      //2.创建一个edges数组
      _this.edges = new Vis.DataSet(_this.edgesArray);
      _this.container = document.getElementById("network");
      _this.data = {
        nodes: _this.nodes,
        edges: _this.edges
      };
      _this.options = {
        autoResize: true, //网络将自动检测其容器的大小调整，并相应地重绘自身
        // locale: "cn", //语言设置：工具栏显示中文
        // //设置语言
        // locales: {
        //   cn: {
        //     //工具栏中文翻译
        //     edit: "编辑",
        //     del: "删除当前节点或关系",
        //     back: "返回",
        //     addNode: "添加节点",
        //     addEdge: "添加连线",
        //     editNode: "编辑节点",
        //     editEdge: "编辑连线",
        //     addDescription: "点击空白处可添加节点",
        //     edgeDescription: "点击某个节点拖拽连线可连接另一个节点",
        //     editEdgeDescription: "可拖拽连线改变关系",
        //     createEdgeError: "无法将边连接到集群",
        //     deleteClusterError: "无法删除集群.",
        //     editClusterError: "无法编辑群集'"
        //   }
        // },

        // 设置节点样式
        nodes: {
          // shape: "circle",
          size: 50,
          font: {
            //字体配置
            size: 24,
          },
          color: {
            // border: "#2B7CE9", //节点边框颜色
            background: "#97C2FC", //节点背景颜色
            highlight: {
              //节点选中时状态颜色
              border: "#2B7CE9",
              background: "#D2E5FF"
            },
            hover: {
              //节点鼠标滑过时状态颜色
              border: "#2B7CE9",
              background: "#D2E5FF"
            }
          },
          borderWidth: 0, //节点边框宽度，单位为px
          borderWidthSelected: 2 //节点被选中时边框的宽度，单位为px
        },
        // 边线配置
        edges: {
          width: 1,
          length: 150,
          color: {
            color: "#848484",
            highlight: "#848484",
            hover: "#848484",
            inherit: "from",
            opacity: 1.0
          },
          shadow: true,
          smooth: {
            //设置两个节点之前的连线的状态
            enabled: true //默认是true，设置为false之后，两个节点之前的连线始终为直线，不会出现贝塞尔曲线
          },
          arrows: { to: true } //箭头指向to
        },
        //计算节点之前斥力，进行自动排列的属性
        physics: {
          enabled: true, //默认是true，设置为false后，节点将不会自动改变，拖动谁谁动。不影响其他的节点
          barnesHut: {
            gravitationalConstant: -4000,
            centralGravity: 0.3,
            springLength: 120,
            springConstant: 0.04,
            damping: 0.09,
            avoidOverlap: 0
          }
        },
        //用于所有用户与网络的交互。处理鼠标和触摸事件以及导航按钮和弹出窗口
        interaction: {
          dragNodes: true, //是否能拖动节点
          dragView: true, //是否能拖动画布
          hover: true, //鼠标移过后加粗该节点和连接线
          multiselect: false, //按 ctrl 多选
          selectable: true, //是否可以点击选择
          selectConnectedEdges: true, //选择节点后是否显示连接线
          hoverConnectedEdges: true, //鼠标滑动节点后是否显示连接线
          zoomView: true //是否能缩放画布
        },
        //操作模块:包括 添加、删除、获取选中点、设置选中点、拖拽系列、点击等等
        // manipulation: {
        //   // enabled: true, //该属性表示可以编辑，出现编辑操作按钮
        //   // addNode: true,
        //   // addEdge: true,
        //   // editNode: undefined,
        //   // editEdge: true,
        //   // deleteNode: true,
        //   // deleteEdge: true
        // }
      };

      _this.network = new Vis.Network(
          _this.container,
          _this.data,
          _this.options
      );
    },

    resetAllNodes() {
      let _this = this;
      _this.nodes.clear();
      _this.edges.clear();
      _this.nodes.add(_this.nodesArray);
      _this.edges.add(_this.edgesArray);
      _this.data = {
        nodes: _this.nodes,
        edges: _this.edges
      };
      //   network是一种用于将包含点和线的网络和网络之间的可视化展示
      _this.network = new Vis.Network(
          _this.container,
          _this.data,
          _this.options
      );
      _this.network.on("click", params => {
        if (params.nodes.length>0) {
          this.entityInfoQuery(params.nodes[0]);
        }
      });
    },
    entityInfoQuery(entityName){
        this.$emit("changeShowedProperty", entityName)
    },
    resetAllNodesStabilize() {
      let _this = this;
      _this.resetAllNodes();
      _this.network.stabilize();
    }
  },

  mounted() {
    this.init();
    // 点击事件
    this.network.on("click", params => {
      // console.log("hell")
      // console.log("点击", params.nodes);
      // this.network.addEdgeMode();
    });
    // 点击鼠标右键事件
    // this.network.on("oncontext", params => {
    //   console.log("右击", params);
    //   this.dialogVisible = true;
    // });
  },
  watch:{
    graphData:{
      handler(newValue, oldValue){
        this.nodesArray = newValue.nodes
        this.edgesArray = newValue.edges
        this.resetAllNodes()
        // this.init()
      },
      deep:true
    }
  }
}

</script>
<style scoped>
</style>