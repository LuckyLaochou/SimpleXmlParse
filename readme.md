# XmlParse
> 这个项目的xml文件是自己定义的，主要用来是通过xml解析生成sql语句，可以满足简单的增删改查，里面使用了很多的stream语法进行解析

## 1. 项目需求：
+ 来自于老师的一些动态生成sql的想法
+ 学长们以前的经验

## 2. 项目技术
+ java语言
+ dom4j 
+ fastjson

## 以后再写

## 测试用例：
```
<view name="queryTeaPutAndDelivery" type="select">
		<tables>
			<table name="t_tea_category">
				<mapping property="name" column="name" />
				<where property="teaName" column="tea_name" />
				<where property="teaClass" column="tea_class" />
			</table>
			<table name="t_put_and_delivery">
				<where property="pdTime" column="pd_time" />
			</table>
			<table name="t_test">
				<where property="pdTime" column="pd_time" />
			</table>
		</tables>
		<conditions>
			<condition value="t_tea_category.name = t_put_and_delivery.name" />
			<condition value="t_tea_category.name = t_put_and_delivery.name" />
		</conditions>
	</view>

	<view name="queryFixationRealTimeData" type="select">
		<tables>
			<table name="t_fixation_real_time_data ">
				<mapping property="productionBatch" column="tea_batch" />
				<mapping property="diviceId" column="divice_id" />
				<mapping property="temperature" column="temperature" />
				<mapping property="humidity" column="humidity" />
				<mapping property="killerSpeed" column="killer_speed" />
				<mapping property="fanSpeed" column="fan_speed" />
				<mapping property="time" column="time" />
				<mapping property="killerStatus" column="killer_status" />
				<mapping property="fanStatus" column="fan_status" />
				<where property="productionBatch" column="tea_batch" />
			</table>
		</tables>
	</view>
```
**生成效果**：
```
SELECT t_tea_category.name FROM t_tea_category,t_put_and_delivery,t_test WHERE t_tea_category.tea_name=? and t_tea_category.tea_class=? and t_put_and_delivery.pd_time=? and t_test.pd_time=? and t_tea_category.name = t_put_and_delivery.name and t_tea_category.name = t_put_and_delivery.name;
SELECT t_fixation_real_time_data .divice_id,t_fixation_real_time_data .fan_status,t_fixation_real_time_data .tea_batch,t_fixation_real_time_data .temperature,t_fixation_real_time_data .humidity,t_fixation_real_time_data .killer_status,t_fixation_real_time_data .killer_speed,t_fixation_real_time_data .fan_speed,t_fixation_real_time_data .time FROM t_fixation_real_time_data  WHERE t_fixation_real_time_data .tea_batch=?;
```
