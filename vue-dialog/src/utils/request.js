import axios from 'axios'

const request = axios.create({timeout:8000})

request.interceptors.request.use(confing=>{
        confing.headers['Content-Type'] = 'application/json;charset=utf-8';

        return confing
    }, error=>{
        return Promise.reject(error)}
);

request.interceptors.response.use(
    response=>{
        let res = response.data;

        if (response.config.responseType==='blob'){
            return res
        }

        if (typeof res==='string'){
            res = res?JSON.parse(res):res
        }
        return res;
    },
    error=>{
        console.log('err', error)}
)

export default request