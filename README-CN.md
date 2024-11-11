# DraMa.InterActivE - DMIAE  
# 一个用于规范剧本的小工具

## 功能：
1. 读取DMIAE，pptx，docx等格式
2. SQLite数据库存储
3. 生成pptx, docx等格式或直接显示
4. Qt图形化界面（待定）

## 使用方法(WIP)：

### 解析文档：

	$ dmiae resolve <format> <file> -n [name]	
将对存储在指定格式（raw, docx, pptx）内的剧本进行解析，转化为数据库记录。  

### 生成文档：

	$ dmiae gen <format> <script> -o <file>	
将对指定名称的剧本编码为对应的格式并输出到指定文件。  

### 展示：

	$ dmiae show <script>
使用内置渲染器对指定剧本进行渲染，渲染参数需在剧本内部以DMIAE属性的方式指定。  
