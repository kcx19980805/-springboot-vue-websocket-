<template>
  <div id="app">
    <!--每个页面的公共部分-->
    <!--这是主窗口，显示当前url的组件-->
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'app',
  components: {
  },
  data(){
    return{
      _beforeUnload_time :'',/*窗口关闭之前的时间*/
      _gap_time:'',/*窗口关闭时的时间*/
    }
  },
  beforeCreate(){//在创造之前数据和方法还未绑定到app上
    //读取sessionStorage里的状态信息重新保存的state中
    if (sessionStorage.getItem('store')) {
      this.$store.replaceState(
              Object.assign(
                      {},
                      this.$store.state,
                      JSON.parse(sessionStorage.getItem('store'))//把字符串解析成json
              )
      )
      console.log(JSON.parse(sessionStorage.getItem('store')))
    }
  },
  mounted() {
    window.addEventListener('beforeunload', e => this.beforeunloadHandler(e))
    window.addEventListener('unload', e => this.unloadHandler(e))
  },
  destroyed() {
    window.removeEventListener('beforeunload', e => this.beforeunloadHandler(e))
    window.removeEventListener('unload', e => this.unloadHandler(e))
  },
  methods:{
    beforeunloadHandler(){
      this._beforeUnload_time=new Date().getTime();
    },
    async unloadHandler(e){
      //判断是窗口关闭还是刷新
      this._gap_time=new Date().getTime()-this._beforeUnload_time;
      //判断是窗口关闭还是刷新,如果窗口关闭，修改用户在线状态
      if(this._gap_time<=5){
        let user=JSON.parse(sessionStorage.getItem('store')).userInfo
        user.isOnline='false'
        let res= await axios.put(this.API.UpdateUser,user)
      }
    },
  }
}
</script>

<style>
  *{
    box-sizing: inherit;

  }
  a {
    text-decoration: none;
    cursor: pointer;
  }
  ul{
    list-style: none;
    margin: 0;
    padding: 0;
  }
  nav,section{
    margin: 0;
    display: block;
  }
  body{
    padding: 0;
    margin: 0;
    font-family: Microsoft Yahei,PingFang SC,Hiragino Sans GB,Helvetica Neue,Helvetica,tahoma,arial,Verdana,sans-serif,WenQuanYi Micro Hei,"\5B8B\4F53";
    font-size: 12px;
  }
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
  }
</style>
