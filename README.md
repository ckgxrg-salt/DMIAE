# DraMa.InterActivE - DMIAE  
# A simple tool to unify scripts for school drama club.

It defines a syntax to define a script, and some utilities to work with this format.   

# Syntax
## Header
The header section defines some important details about the script itself.   

### Characters
Declare all characters with the *@* prefix，one per line.   
If necessary, colons(:) can be used after character names to write descriptions.   
For characters with multiple aliases, use commas(,) to separate each alias.   
Any characters that appears in the script must be declared, unless they'll be identified as part of other characters' line.   
Character names that declared in the header should be **EXACTLY THE SAME** as they appear in the script.   
If there are spell errors，**ALL** names, even misspelled ones, should be declared.   
It's better to avoid name ambiguities, for example, don't just refer to the first name when two characters both have it.   
DMIAE, by default, uses the first defined name of character.(To avoid ambiguities)   
*All, Everyone* and such is declared as fallback, you can also manually declare them again.   
```
@Hamilton:the main character	//OK
@Hamilton,HAMILTON,HAMILTON XXX	//If you refer to Hamilton in multiple ways this is compulsory.
@Elle,Legally Blonde	//OK
@Chief Justice a maniac	//Invalid，The name will be identified as "Chief Justice a maniac"
```   

### Properties
Set properties of the script with the *#* prefix.   
Properties are case-insensitive.   
A property is a key-value pair, key and value is separated with a comma(,).   
For example, the background of the generated slideshow can be set.   
*[]* is used to limit the property in a domain.   
```
#sublineFormat: 0, 1	//OK
```   	

### Property Domains
Each property has its own domain, so it is only effective under certain conditions.   
Property domains are in the form *`#[key = value, key = value, ...] property`*.   
A property will be applied when all of the assertions are satisfied.   
By default, all properties have the domain *`[true = true]`*.   
For example:   
```
#[lineType = subline, index = 1] halt: true
```   
will halt DMIAE when it reads 2 consecutive sublines.   
The keys are flags that reflect DMIAE's working status.   
TODO: Give full list of keys   

Finally, use #END to end headers and begin the content.   

## Content
There should be a clear separation between character names and their lines, colons, commas, spaces and such are acceptable.   
Letters, in any form, is not accepted.   
If DMIAE doesn't identify a explicitly written character name, it treats this as the continuation of lines of previous character.   
So don't forget to explicitly change the character.   
```
Elle: I feel so much better,
I feel so much better,
Elle
	I feel so much better than before	//OK

Burr How does a bastard, son of a whore	//Not recommended, since it's ambiguous whether Burr is the speaker or just mentioned by the real speaker.

Emmett: Don't spend hours on my hair
(I don't spend hours)	//Invalid，since this line is Elle's, Elle's name must be clearly written.
```   

### Multiple Characters
If a line is for multiple characters, use slash(/) to separate them.   
Never write the same for the same character multiple times.   
You could write different alias of the same character.(Man, who'll do that)   
```
HAMILTON/MULLIGAN/LAURENS/LAFAYETTE
I am not throwing away my shot.
我绝不放过良机	//OK，but all names above must be declared, otherwise the next part will be identified as part of the line.
	
HAMILTON/MULLIGAN/LAURENS LAFAYETTE
//But not this，DMIAE will treat "LAURENS LAFAYETTE" as a single character，causing errors.
```   

### Sublines
You can attach lines to other lines（Translations are done by this feature)   
The attached lines are called Sublines.   
By default they inherit their attached line's character.   
However you can also explicitly indicate a character(TODO).   
Use *<:* or *:* to attach to previous main line. Use *>:* to attach to next main line.   
If sublines follow a certain format: for example in translation.   
Then you can set the property *#sublineFormat=0,1*, which means for each main line there are 0 sublines attached previously and 1 attached afterwards.   
```
>:全体目光向我看齐，我宣布个事！	//OK
Carlos: Peoples, I have a big announcement!

Hamilton: Alexander Hamilton.
<:Alexander Hamilton
My name is Alexander Hamilton.
:我的名字是Alexander Hamilton。	//OK

#sublineFormat=0,1
#END
All: Gay or European?
基佬还是欧洲人？
It's hard to guarantee.
这可不好确认。	//OK
```   

### Annotations
You can add Annotations to the lines，like lighting，music scripting，or just notes.   
Use *<@* to add annotation for the previous line. Use *@* or *>@* to add for the next line.   
Annotations are in the format *<@ : Type of Annotation : Content>*   
If Type is left blank it will be seen as a note.   

Types of Annotation:   
1. Lighting：LIGHTING LIGHT LT L
2. Nose：NOTE NT N
3. Audio：AUDIO MUSIC SOUND A
4. Character: CHARACTER CHARA CH C
```
@:L:红1面2
All: Blood in the water.
:水中之血。
@:NT:开始逼近
Callahan: Becomes your only law.
:成为你唯一的法则。
<@:Callahan关门
<@:AUDIO:Blood in the water结束
```   

### Forced Main Line
Two consecutive colons(::) forcedly mark the line as a main line, bypassing detection of sublines and annotations.    
If there are a few lines that does not follow format set in sublineFormat, you can utilise this.   
sublineFormat will not count the forcedly marked main line.   
So it's recommended to forcedly mark the next line after a single line as main line.   
```
#sublineFormat=0,1

you never learned to take your time!
你急不可待
Oh, Alexander Hamilton		//This is a single line.
::When America sings for you	//Without a forced mark, this will be identified as a subline according to the format.
美利坚歌颂你时
Will they know what you overcame?
会否知晓  你的辉煌经历
```   

### Runtime Flags
The Parser accepts runtime flags with prefix *#*.   
(TODO)
```
#sublineFormat=0,1
Callahan: Ms... Elle...
嗯……Elle……
#sublineFormat=0,0
Elle: Woods! Elle Woods.
#sublineFormat=0,1
Callahan: Well, someone had had their morning coffee.
嗯，有人一大早就精力充沛啊。 
```   

# Example
An example of script that follows DMIAE syntax:   
```
#name:Legally Blonde
#author:forgot
#credit:Birch
	
@Elle
@Warner,Warner Huntington IV
	
#sublineFormat:0,1
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
```   

# Usage(Currently implemented)
Call `dmiae parse` to read a file and store as a DMIAE raw file.   
```
$ dmiae parse <path to file> [-o/--output path to store result] [-v/--verbose]
```

# See
I plan to also implement this in Rust, but I haven't started yet.   

# Work In Progress
TODO: Impl PropDomain
TODO: Add Plain Generator
