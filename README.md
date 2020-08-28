# Proj4_Kotlin_Ver「乗り過ごし防止アプリ」

***

## 概要

うとうとしたりスマートフォンに夢中になるあまり、降りるべき駅を通り過ぎることを防止するためのアプリケーションです  
ユーザーが降りる駅と通知してほしいタイミング(駅から何メートル)を設定すると、HeadsUp通知でお知らせします。また、ユーザーが有線・無線イヤホンなどをしている場合に限り、イヤホンから事前に設定したアラームでお知らせします。

***
## 主な画面

より良い画面・機能にするため変更中

***

## 使用した技術・ライブラリ

- [ジオフェンス](https://developer.android.com/training/location/geofencing?hl=ja)
  - このアプリのコアとなるものです。範囲内に入ったこと通知してくれたり位置情報の取得を勝手にしてくれるもので、便利ですが問題もありWifiや基地局から位置情報を取得できないため取得が遅い、できない等の問題があります。

- [Fuel](https://github.com/kittinunf/fuel)
  - HeartRailsAPIから路線情報、駅情報を取得する際に使用しました。またJSONのデシリアライズもできるため路線情報のリスト化はこちらで行いました。

- [Jackson](https://github.com/FasterXML/jackson-module-kotlin)
  - 駅情報のJSONは入れ子になっており情報も多かったので、より簡単にリスト化できるJacksonを用いました。

- [EventBus](https://github.com/greenrobot/EventBus)
  - ジオフェンスが指定範囲内に入ったときにBroadcastReceiverがそのイベントを受け取りますが、HeadsetPlugReceiverのようにコンテキストをキャストできなかったのでEventBusを用いてGeofenceServiceに通知しています。

- [oss-licenses-plugin](https://github.com/google/play-services-plugins/tree/master/oss-licenses-plugin)
  - 上記のライブラリ等を一括でリスト化してくれるものです。

***

## 注意

GithubにあがっているものをそのままクローンしてもAPIキーがないので動きません。