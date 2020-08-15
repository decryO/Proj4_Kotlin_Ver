# Proj4_Kotlin_Ver「乗り過ごし防止アプリ」

***

## 概要

うとうとしたりスマートフォンに夢中になるあまり、降りるべき駅を通り過ぎることを防止するためのアプリケーションです  
ユーザーが降りる駅と通知してほしいタイミング(駅から何メートル)を設定すると、HeadsUp通知でお知らせします。また、ユーザーが有線・無線イヤホンなどをしている場合に限り、イヤホンから事前に設定したアラームでお知らせします。

***
## 主な画面

基本となる画面です。ユーザーは初めての起動時は位置情報の取得の許可が求められます

<img src="/images/main_1.jpg" width="25%" /><img src="/images/main_2.jpg" width="25%" /><img src="/images/station_select.jpg" width="25%" />

活性となっているボタンを押すと都道府県や、その都道府県にある路線などの一覧がダイアログで出現します。Map下にあるスライダーはユーザーが通知してほしいタイミングを設定するもので、駅からどれだけ離れている場所での通知か分かるようにMapに円を描画しています。メニューを開くとアラームの設定や使用したOSSの一覧、駅情報を取得させていただいているHeartRails様の情報が記載された画面を表示します。

<img src="/images/alarm_set.jpg" width="25%" />

アラームセットボタンを押下すると何処の駅にアラームをセットしたかが通知されます。この通知はForegroundServiceの通知なのでスワイプ等では消せません。通知をタップするとMainActivityが起動され、アラームを停止するボタンが表示されます。  

<img src="/images/alarm_active.jpg" width="25%" />

指定した駅に接近するとHeads-Up通知でお知らせします。この通知は時間経過等では消えず、ユーザーが停止ボタンを押す、もしくはアプリを開いて停止ボタンを押さない限りで続けます。ヘッドフォン等している場合はアラーム音が鳴動し続けます。

***

## 使用した技術・ライブラリ

- [ジオフェンス](https://developer.android.com/training/location/geofencing?hl=ja)
  - このアプリのコアとなるものです。範囲内に入ったこと通知してくれたり位置情報の取得を勝手にしてくれるもので、便利ですが問題もありWifiや基地局から位置情報を取得できないため取得が遅い、できない等の問題があります。

- [Fuel](https://github.com/kittinunf/fuel)
  - HeartRailsAPIから路線情報、駅情報を取得する際に使用しました。またJSONのデシリアライズもできるため路線情報のリスト化はこちらで行いました。

- [Jackson](https://github.com/FasterXML/jackson-module-kotlin)
  - 駅情報のJSONは入れ子になっており情報も多かったので、より簡単にリスト化できるJacksonを用いました。

- EventBus
  - ジオフェンスが指定範囲内に入ったときにBroadcastReceiverがそのイベントを受け取りますが、HeadsetPlugReceiverのようにコンテキストをキャストできなかったのでEventBusを用いてGeofenceServiceに通知しています。

- [oss-licenses-plugin](https://github.com/google/play-services-plugins/tree/master/oss-licenses-plugin)
  - 上記のライブラリ等を一括でリスト化してくれるものです。

***

## 注意

GithubにあがっているものをそのままクローンしてもMapは表示されません。