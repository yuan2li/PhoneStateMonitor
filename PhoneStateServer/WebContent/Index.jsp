<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!-- 实现采集的手机实时状态信息的图形界面显示以及采集周期的管理员配置功能 -->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>手机运行状态监测系统</title>
<!-- 引入echarts和jquery库的js文件 -->
<script type="text/javascript" src="js/echarts.js"></script>
<script type="text/javascript" src="js/jquery-3.4.1.min.js"></script>
</head>
<body>
<div id="column" style="width:1500px; height:100px">
	<div id="select" style="width:100px; height:100px; margin-left:50px; margin-top:50px; float:left">
	<!-- 采集周期配置下拉列表 -->
	<p>采集周期配置</p>
	<select id="select_time">
		<option value="5000">5s</option>
		<option value="10000">10s</option>
		<option value="15000">15s</option>
		<option value="30000">30s</option>
	</select></div>
	<!-- 折线图显示 -->
    <div id="main" style="width:1000px; height:500px; margin-left:auto; margin-right:auto; float:left"></div>
</div>
	<!-- 文本显示 -->
    <div id="line" style="width:800px; height:200px; margin-left:auto; margin-right:auto"></div>
    <!-- 处理数据交互和图形绘制的脚本 -->
    <script type="text/javascript">
        // 基于准备好的dom，初始化echarts实例
        var myChart = echarts.init(document.getElementById('main'));
        // 指定图表的配置项
        var option = {
            title: {
            	x: '20',
                text: ''
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
            	right: '20',
                data:['剩余RAM(GB)','总RAM(GB)','剩余ROM(GB)','\n','总ROM(GB)','信号强度(-dBm)','电池电量(%)']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: []
            },
            yAxis: {
        		type: 'value'
            },
            series: [
                {
                    name:'剩余RAM(GB)',
                    type:'line',
                    data:[]
                },
                {
                    name:'总RAM(GB)',
                    type:'line',
                    data:[]
                },
                {
                    name:'剩余ROM(GB)',
                    type:'line',
                    data:[]
                },
                {
                    name:'总ROM(GB)',
                    type:'line',
                    data:[]
                },
                {
                    name:'信号强度(-dBm)',
                    type:'line',
                    data:[]
                },
                {
                    name:'电池电量(%)',
                    type:'line',
                    data:[]
                }
            ]
        };
        
		/* 处理x轴标签（即采集时间）文字自动换行显示（日期/n时间） */
        option.xAxis.axisLabel = {
            interval: 0, //标签设置为全部显示
            formatter: function (params) {
                var newParamsName = ""; // 最终拼接成的用于显示的标签字符串
                var paramsNameNumber = params.length; // 标签字符串的原始长度
                var provideNumber = 10; // 每行显示的字符个数（xxxx-xx-xx）
                var rowNumber = Math.ceil(paramsNameNumber / provideNumber); // 字符串分行后的行数
                if (rowNumber > 1) {
                    for (var p = 0; p < rowNumber; p++) {
                        var tempStr = ""; // 表示每一次截取的字符串
                        var start = p * provideNumber; // 开始截取的位置
                        var end = start + provideNumber; // 结束截取的位置
                        if (p == rowNumber - 1) {
                            // 此处对最后一行的索引值单独处理，截取到原始字符串的结束位置
                            tempStr = params.substring(start, paramsNameNumber);
                        } else {
                            // 每一次拼接字符串并换行
                            tempStr = params.substring(start, end) + "\n";
                        }
                        newParamsName += tempStr; // 拼接得到最终用于显示的字符串
                    }

                } else {
                    // 若标签字符串可显示在一行内，则将旧标签的值直接赋给新标签
                    newParamsName = params;
                }
                return newParamsName;
            }
        }
		
		var interval = "5000"; //设定初始采集周期
		var loadTime = 5000; //设定初始网页刷新周期
		/* 获取下拉列表的值，更新采集周期和网页刷新周期，页面刷新保持下拉框内容不变 */
		$("#select_time").on('change', function(){
        	interval = document.getElementById("select_time").value;
        	loadTime = parseInt(interval);
        	localStorage.setItem('select_time', $('option:selected', this).index());
		});
      	//加载数据时显示加载动画
		myChart.showLoading();
      	
		//自动刷新页面
		/*第一次读取最新通知*/
  		setTimeout(function() {
  			Update();
        }, 2000);
      	/*页面重载*/  	
        var set = setInterval(ReLoad, loadTime);
        function ReLoad() {
        	if (localStorage.getItem('select_time')) {
                $("#select_time option").eq(localStorage.getItem('select_time')).prop('selected', true);
            }
        	Update();
        	//关闭当前定时器，更新网页刷新周期后重启
            clearInterval(set);
            set = setInterval(ReLoad, loadTime);
        }

      	/* 与服务器进行数据交互 */
		function Update(){
			//定义数据存放数组
			var id = [];
			var time = [];
			var aram = [];
			var tram = [];
			var arom = [];
			var trom = [];
			var ss = [];
			var bp = [];
			//ajax发起数据请求
	        $.ajax({
				type : "get", //数据请求方式为get
				async : true, //异步请求
				url : "MyHttpServer", //将要访问的web服务器地址
				data : {intervalTime : interval}, //向服务器发送管理员配置的最新采集周期值
				dataType : "text",
				success : function(result) {
					var html = "<ul>"
					//解析获取的Json字符串
					result=JSON.parse(result);
					if (result) {
						//单独获取采集的手机标识
						id = result[0].phoneID;
						//遍历结果集
						$.each(result, function(i,n) {
							//迭代取出各项数据放入对应的数组中
			                time.push(n.recordTime);
			                aram.push(n.availRAM);
			                tram.push(n.totalRAM);
			                arom.push(n.availROM);
			                trom.push(n.totalROM);
			                ss.push(n.signalStrength);
			                bp.push(n.batteryPower);
			                //以文本方式显示地理位置信息
			                html += "<li>[时间] " + n.recordTime + "   [经纬度]";
			                html += n.latitude + "°N " + n.longitude + "°E   [地址]";
			                html += n.address + "</li>";
			            });
						myChart.hideLoading(); //隐藏加载动画
						html += "<ul>";
						$("#line").html(html); //显示html内容
						//根据数据加载数据图表
						myChart.setOption({
				            title: {
				                text : id
				            },
							xAxis : {
								data : time
							},
							series : [
								// 根据名字对应到相应的数据
								{
									name : '剩余RAM(GB)',
									data : aram
								},
								{
									name : '总RAM(GB)',
									data : tram
								},
								{
									name : '剩余ROM(GB)',
									data : arom
								},
								{
									name : '总ROM(GB)',
									data : trom
								},
								{
									name : '信号强度(-dBm)',
									data : ss
								},
								{
									name : '电池电量(%)',
									data : bp
								}
							]
						});
					}
				},
				error : function(errorMsg) {
					//请求失败时执行该函数
					alert("Data request failure!");
					myChart.hideLoading();
				}
			})
		}
    </script>
</body>
</html>