
* * * * * * *
HTML記述ポリシー
* * * * * * *

※sample.htm を参考に

[Title]
title = ページのタイトル(h1と同じ値) | DBFlute

<title>ConditionBeanのインストール | DBFlute</title>


[Heading]
h1 = ページのタイトル
h2 = カテゴリの見出し
h3 = サブカテゴリの見出し
h4 = サブサブカテゴリの見出し
h5 = あんまり利用しない(HTMLを分割する)

h1直下には、indexlistを付与する。
<h1>foo</h1>
${indexlist}

h1直下には、そのページの内容がタイトルからだけでは
あまりイメージが湧かないような場合に補足として文章を付ける。
<h1>foo</h1>
<p>
	Who is foo?
</p>
${indexlist}


[Paragraph]
文章は必ず "pタグ" で囲う(ddとか以外)

<h2>ConditionBeanの機能</h2>
<p>
    ConditionBeanには変な機能がたくさんあります。
    その理由は、作者がそれなりに変な人で...
</p>

文章の中でリンクを(あまり)作らない。
リンクは基本的に "Detail Page" や "Related Page" を利用。


[Unordered List (ulタグ)]
フローを表すようなリスト、かつ、それぞれ詳細へのリンクが
あるような場合は、"flowlist" を利用。
(詳細(リンク)がないような場合は ol でもOK)

<ul class="flowlist">
	<li><a href="#">1. お湯を沸かす</a></li>
	<li>2. 熱いと叫ぶ</li>
	<li>3. ...</li>
</ul>

codeタグの補足に使用する場合は、"codeoutro" を利用。
</code></pre>
<ul class="codeoutro">
	<li></li>
</ul>

dlのddでの補足リストは...詳しくは "Description List" にて。

それ以外の、ただのリストなら、特にクラス属性指定無し。


[Description List (dlタグ)]
必ずclass属性を指定。
ddに文章がじゃんじゃん来る(割と普通のdl)：class="textlist"
横並びでdtがメインでキーになる：class="keymainlist"
横並びでddがメインで値っぽい感じ：class="valuemainlist"
キーメインでちょっとした補足リスト：class="compactlist"
ネスト構造バリバリなリスト：class="hierarchylist"
(ddは一行でネスト構造じゃんじゃん)

ddの中で箇条書きで補足的なことを書くときは：
<dd>
	<ul class="supplementlist">
		<li>...</li>
	</ul>
</dd>


[Preformatted Text & Code (pre, code)]
インデントなしで左側にべちゃっと付ける。
必ずタイトルをつけること。最後に "@" でどういう領域のコードなのか説明。

<pre><span class="codetitle">ex) 名前が'S'で始まる会員をリスト検索 @Java</span><code>
MemberCB cb = new MemberCB();
cb.query().set...
</code></pre>

<pre><span class="codetitle">ex) dbflute.diconの独自のファイル名を指定 @dependencyInjectionMap.dfprop</span><code>
; dbfluteDiconFileName = dbflute-foodb.dicon
</code></pre>

<pre><span class="codetitle">ex) Mavenプロジェクトの作成コマンド @Command</span><code>
mvn archetype:create \
    -DgroupId=グループID \
    -DartifactId=プロジェクトID \
    -DarchetypeArtifactId=maven-archetype-webapp
</code></pre>

span.comment : コメントに利用 (Javaなら /**/ | // 、SQLなら /**/ | -- など)
span.keyword : コードの構文の利用 (Javaなら public | static | new 、SQLなら select | where など)
span.literal : リテラル表現に利用 (Javaなら "foo" | 123 、SQLなら 'foo')
span.attribute : 属性の利用 (Javaならインスタンス変数(xxxBhv)、その他属性っぽいものに柔軟に利用)
span.dbtype : DB型に利用 (Javaではほとんど利用することない、SQLなら create 文とか)
span.point : 対象に問わず、今回ここを強調したい、と思うところ
span.abbreviation : ドキュメント上の省略を表現する時に利用。"..." などに付与


[Detail Page]
"詳細ページはこちら" みたいなときは、文章で表現するのではなく、
divタグでdetailpageを指定したリンクで表現する。
(CSSで詳細へのリンクであることが装飾される)
タイトルだけだとどのページがよくわからないときに、
どこどこのなになにという感じで " - " で連結したリンクにする。

<h3 id="property">特定環境適用プロパティ</h3>
<p>
	最初の自動生成の前に考慮すべきある特定の環境に適用するためのプロパティを設定して下さい。
</p>
<div class="detailpage"><a href="./firstprop.html">セットアップ - 特定環境適用プロパティ</a></div>


[Related Page]
関連ページに関して、詳細ページでの扱いとほぼ同じ。
style名が "relaltedpage" となる。
タイトルだけだとどのページがよくわからないときに、
どこどこのなになにという感じで " - " で連結したリンクにする。

<dt>Maven DBFlute Pluginが動作可能であること</dt>
<dd>
	Maven DBFlute Plugin はDBFluteの支援機能を備えたMavenプラグインです。
</dd>
<div class="relpage"><a href="../../manual/function/helper/maven/install.html">Maven DBFlute Pluginのインストール</a></div>


[Line Separator]
文章を管理上改行(brじゃない改行)するときは、句読点やクォーテーション""、空白の境目ですること。
brによる改行は基本的には利用しない。


[em]
単語もしくは文章を強調するときは、<em class="keyword"></em>で囲む。
その文章全体を要約するようなキーワードがある場合に付与する(視覚的に理解できるようにする)。
その場合、基本的には前後に半角空白を空ける。
但し、文章の始まりや終わり、直前・直前が句読点など、空白っぽい表現と隣接する場合は、空白は不要。
また、管理上改行したい際は、後ろの空白はなしで直後で改行するようにする(HTML空白問題の回避)。


[Quotation]
空白を含む連語や短すぎる表現など、感覚的に単語のまとまりがわかりづらい
と書いてる人が思った場合に、ダブルクォーテーション""で囲む。
ただし、emで囲む範囲と一致する場合はダブルクォーテーションは不要。
  ex) "maven dbflute - plugin"
  ex) ";"
その場合、基本的には前後半角空白を空ける。
但し、文章の始まりや終わり、直前・直前が句読点など、空白っぽい表現と隣接する場合は、空白は不要。
また、管理上改行したい際は、後ろの空白はなしで直後で改行するようにする(HTML空白問題の回避)。


[Other]
o 基本的にインデントはタブ
o 基本的に空行を空けない (但し、h2タグの直前とinucolumnのdivタグの直前・直後は空ける)
