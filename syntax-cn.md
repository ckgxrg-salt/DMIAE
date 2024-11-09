# DMIAE语法:

## 剧本起始段：

以*@*开头，声明所有角色，每个一行  
如果需要，在角色名后加：来追加描述  
如果角色有多个别名，使用，将它们隔开  
任何在剧本中出现的角色名都应当被声明，否则将被解析为全体台词  
声明的角色名应当与在剧本中称呼该角色的方式**完全一致**  
如果没能保证完全一致，那么该角色出现时**每个**使用过的名字都要被声明  
不要出现有歧义的名字，如有同名不同姓的角色，那么不要简写名  
DMIAE生成文档时会默认以最长的名字称呼角色  
*All, Everyone*类似保留词已经预先声明，可以忽略  
当然也可以自己声明一遍

	@Hamilton:the main character	//OK
	@Hamilton,HAMILTON,HAMILTON XXX	//如果称呼方式不同，必须这么做
	@Elle,Legally Blonde	//OK
	@Chief Justice a maniac	//非法，角色名会被识别为Chief Justice a maniac
	
以*#*开头，指定该剧本的属性
属性不区分大小写  
属性是键值对，键与值之间用冒号区分
如生成演示文稿时其背景等  
使用*[]*指定该属性的作用域  

	#ppt.Fill:0, 0, 0	//OK
	#[Subline 1]ppt.FontSize:36
	#TolerateCNChar	//加入这行以容忍使用全角字符进行声明操作等
	
随后，使用*#END*来结束起始段，开始正文  
	
## 剧本主体段：

角色名与正文间应当有分隔符，常见的形式如冒号，回车，空格  
不可以使用字母或汉字作为分隔符  
再次强调角色名应当与声明中的角色名完全一致  
全体台词可以使用All, Everyone等保留字  
DMIAE顺次检索角色名，发现一个角色名后，直到下一个角色名出现前的台词都会被认为是该角色的台词，因此对白中注意不要漏写角色名  

	Elle: I feel so much better,
	I feel so much better,
	Elle
		I feel so much better than before	//OK

	Burr How does a bastard, son of a whore	//可以但不推荐，因为会有歧义，需要手动确认

	Emmett: Don't spend hours on my hair
	(I don't spend hours)	//非法，Elle的台词必须指定Elle名字在前

多个角色合唱的台词之前的角色名应用斜线(/)将各个角色分开
每个角色的每个名字不可以重复出现，否则格式会混乱
同一角色的不同名字可以多次出现(但是谁会那么干）

	HAMILTON/MULLIGAN/LAURENS/LAFAYETTE/向奕帆/吴小有/孙子璇/严周隽
	I am not throwing away my shot.
	我绝不放过良机	//可以，但是注意这样需要声明@HAMILTON,向奕帆，否则DMIAE会将后面部分识别为台词的一部分
	
	HAMILTON/MULLIGAN/LAURENS/LAFAYETTE 向奕帆/吴小有/孙子璇/严周隽
	//但是这样不可以，DMIAE会将LAFAYETTE 向奕帆认为是一个角色，从而引起多重角色未声明报错

对台词的附加台词（使用该功能追加翻译）  
附加台词不必再声明角色  
使用*<:*或者*:*代表附加于前方，*>:*附加于后方  
如果附加台词遵循特定格式：如，两行台词为一组（尤其用于翻译中）  
则可以通过声明段指定属性*#SublineFormat=0,1* : 对于每一行台词，前方有0行附加台词，后方有1行附加台词  

	>:全体目光向我看齐，我宣布个事！	//OK
	Carlos: Peoples, I have a big announcement!

	Hamilton: Alexander Hamilton.
	<:Alexander Hamilton
	My name is Alexander Hamilton.
	:我的名字是Alexander Hamilton。	//OK

	#SublineFormat=0,1
	#END
	All: Gay or European?
	基佬还是欧洲人？
	It's hard to guarantee.
	这可不好确认。	//OK
	
对台词的注解，如添加灯光，音乐进入时机，笔记等  
使用*<@*代表注解在前方的台词上，*@*或者*>@*注解在后方的台词上  
注解的格式是*<@ : 注解类型 : 内容*  
若注解类型省略则默认为笔记型注解  

所有注解类型：
1. 灯光：LIGHTING LIGHT LT L
2. 笔记：NOTE NT N
3. 音频：AUDIO MUSIC SOUND A
4. 

	@:L:红1面2
	All: Blood in the water.
	:水中之血。
	@:NT:开始逼近
	Callahan: Becomes your only law.
	:成为你唯一的法则。
	<@:Callahan关门
	<@:AUDIO:Blood in the water结束
	
连用两次冒号::以强制标记该句为主要台词，屏蔽一切附加台词或注解检测
如果出现比较稀少的不符合指定的SublineFormat属性的台词与附加台词，可以利用
::强制指定主句，注意强制指定的台词不会被SublineFormat属性计数
因此推荐在单句后的下一主句加入强制主句

	#SublineFormat:0,1

	you never learned to take your time!
	你急不可待
	Oh, Alexander Hamilton		//这是一个单句
	::When America sings for you	//如果不加入强制主句声明, 这句会被格式化地识别为附加台词
	美利坚歌颂你时
	Will they know what you overcame?
	会否知晓  你的辉煌经历

DMIAE解析器标记符号为*#*  
使用解析器标记符号以标记一些DMIAE解析器应当特殊对待的地方。  
运行时的标记与起始段的全局标记无本质区别，但是当DMIAE解析器读取该行时才会被执行。  
例如，当剧本中出现单纯说角色名字的语句，因此无需翻译时：

	#SublineFormat=0,1
	Callahan: Ms... Elle...
	嗯……Elle……
	#SublineFormat=0,0
	Elle: Woods! Elle Woods.
	#SublineFormat=0,1
	Callahan: Well, someone had had their morning coffee.
	嗯，有人一大早就精力充沛啊。
不过，这么费劲还不如打个空行或者把名字抄一遍  
	
一段遵循DMIAE语法的剧本示例:  

	#Name:Legally Blonde
	#Author:forgot
	#Credit:Birch
	
	@Elle
	@Warner,Warner Huntington IV
	
	#SublineFormat:0,1
	#END
	
	@:L:粉2面1
	Elle    Oh Warner, tonight is just perfect.
	哦，Warner,今夜真是太完美了。
	Warner Huntington IV: No, it's you're perfect.
	不，完美的是你。
	Elle.........No, it's you.
	不，是你。
	Elle You, you, you, okay, I'm irritating myself.
	你，你，你，好吧，我自己都烦了。
	@:A:Serious进
	Warner+I have some dreams to make true
	
	
	
	我有一些美梦要去实现
	That's why you and me,
	这就是为什么你我……
	Should break up!
	应该分手！
	<@:A:Serious结束
	<@:干脆一点
	@:L:蓝2面1
	Elle: Okay baby I'll give my han...What?
	好呀，这是我的手……什么！
