# microservice-sample
このリポジトリはSpringCloudのコンポーネントを中心に構成されるマイクロサービスのサンプルです。

[microservice-frontend-sample](https://github.com/masakiii03/microservice-frontend-sample)(別リポジトリ)から呼び出すことを前提としています。

security面はGithub OAuth を利用した認可コードフローで認可を行っています。そのため、事前にGithub OAuthの作成も必要です。

## 使用コンポーネントと概要

### Eureka
- サービスディスカバリ
- サービスレジストリとクライアントから構成されている
  - サービスレジストリ
    - マイクロサービスを管理する
  - クライアント
    - サービスレジストリに自身の情報を登録する
    - リクエスト送信時にサービスレジストリからリストを取得してサービスを特定できる(サービス間通信、ロードバランシングの実現)

### Spring Cloud OpenFeign
- RESTful API を使用するためのサービスで宣言的RESTクライアント
- サービスディスカバリに登録されているサービス名を指定することで、サービス間呼出しが実現可能
- Spring MVCと同じアノテーションが利用できる(`@RequestMapping`, `@GetMapping`)

### Spring Cloud Load Balancer
- 負荷分散
- カスタムロードバランサーを実装することでルーティングを制御できる(カナリアリリース)

### Resilience4j
- サーキットブレーカー
- サーキットブレーカーには以下の3つの状態がある
  - CLOSED
    - 通常の状態
  - OPEN
    - サーキットブレーカーが起動して、通信が行われない状態
  - HALF_OPEN
    - OPENからCLOSEDに状態が戻る途中段階で、リクエストの状態によってCLOSEDに戻る

### Spring CLoud Gateway
- API Gateway
- ルーティング時に負荷分散できる
- パス、ヘッダー、HTTPメソッドなどでルーティングを制御可能
- ルーティングのweightの設定も可能

### Spring Cloud Config
- 設定ファイルをGitリポジトリなどに外出しして、configサーバーで一括管理する仕組み
- configのクライアントは`/actuator/refresh`にPOSTアクセスをするとconfigサーバーの設定値を読み込み直すため、設定ファイルを書き換える度にサービスを再起動する必要がなくなる
- `@ConfigurationProperties`
  - refreshで設定値がリロードされる
- `@Value`
  - refreshをしても設定値はリロードされない
  - クラスに`@RefreshScope`アノテーションを付与するとrefresh時に設定値がリロードされる

### Spring Cloud Bus × RabbitMQ
各サービスのプロパティを一括refreshする仕組み
- Spring Cloud Bus
  - 複数アプリ間でのイベント通知とメッセージングをサポートするツール
  - Spring Cloud Configと組み合わせて使用すると設定の変更をAMQPを介してイベントを送受信できる
- RabbitMQ
  - AMQPを使用してメッセージの送受信を接続する
  - メッセージキューを利用してアプリ間の非同期通信をおこなう仕組み

### Spring Security
- Spring ベースのアプリケーションを保護する標準フレームワーク
- 認証認可、アクセス制御が可能

### Micrometer × Zipkin
- 分散トレーシング
- 一つのトランザクションで複数マイクロサービスを跨ぐ場合に、サービスごとのパフォーマンスの確認やボトルネックを発見できる

- Micrometer
  - アプリのメトリクス収集ライブラリ
  - レスポンス時間、エラー、リクエスト数などを収集する
- Zipkin
  - 分散トレーシングシステムの一つ
  - 実装(コーディング)は不要で、設定追加のみでトレーシングをおこなえる
  - マイクロサービス間のリクエストフローを視覚化してそれぞれのサービスでの処理時間を把握できる
  - 以下で構成されている
    - トレーシングをおこなうトレーサー
    - トレース結果を確認するZipkinサーバー
  - 本来、トレースデータをDBに保存する必要があるが、テスト用にメモリ上にトレースデータを保存できる

必要な依存関係
- io.zipkin.reporter2:zipkin-reporter-brave
  - リクエストが発生した際にトレースや処理時間を収集する
- io.micrometer:micrometer-tracing-bridge-brave
  - `micrometer-tracing-bridge-brave`で収集した情報をZipkinに送信する
- io.github.openfeign:feign-micrometer
  - `MicrometerCapability`のBean作成に必要
  - `MicrometerCapability`のBeanはOpenFeignでのリクエストをMicrometerでモニタリングするために必要。
  このBeanがないとSpanIdごとに新しくTraceIdが生成されてしまう

## その他機能

### カナリアリリース
gatewayでカナリアリリースの仕組みを実装しています。

実装内容
- gateway
  - configで新バージョンのweightを設定(`new-version-weight`)
  - Spring CLoud LoadBalancer でカスタムロードバランサーを作成
    - weightをかけてサービスにルーティングさせる
  - [config-repo](https://github.com/masakiii03/config-repo)の設定ファイルにある`new-version-weight`を変更してrefreshすることで、weightを変更可能
- gatewayからルーティングされるサービス
  - configで`metadata-map.version`を設定、Eurekaに登録
  - Spring CLoud LoadBalancer でカスタムロードバランサーを作成
    - サービス間通信時は自サービスと同バージョンの他サービスを呼び出すよう実装
- 他サービス
  - configで`metadata-map.version`を設定、Eurekaに登録

### 分散トランザクション
一部のサービスでTCCパターンの分散トランザクションを実装しています。

実装内容
- `client-1`にプロダクト情報のDB、`client-2`に口座情報のDBを作成
- プロダクト購入処理を実装
- tryフェーズ
  - `client-1`のtryフェーズ
    - プロダクトの在庫確認
    - `reservedQuantity`に購入予定数を登録
  - `client-2`のtryフェーズ
    - 口座の残高を確認
    - `reservedBalance`に購入予定金額を登録
- confirmフェーズ
  - `client-1`のconfirmフェーズ
    - `reservedQuantity`を'0'にして、`quantity`から購入予定数を引いて登録
  - `client-2`のconfirmフェーズ
    - `reservedBalance`を'0'にして、`balance`から購入予定金額を引いて登録
- cancelフェーズ
  - `client-1`のcancelフェーズ
    - confirm前の場合、`reservedQuantity`を'0'に戻す
    - confirm後の場合、`quantity`を元に戻す
  - `client-2`のcancelフェーズ
    - confirm前の場合、`reservedBalance`を'0'に戻す
    - confirm後の場合、`balance`を元に戻す

## システム構成
![microservice](./microservice.drawio.svg)

- Eureka Server
  - サービスディスカバリー
- authentication-service
  - Github OAuth を使った認証・認可をおこなう
  - gateway, client-1 ~ 4へのアクセス時にSpring Securityでアクセストークンを認証する
- gateway
  - パスによって`client-1`, `client-3`にルーティングを振り分け
- client-1
  - OpenFeign経由で`client-2`のメソッドを呼び出す
  - `client-2`呼び出し処理でサーキットブレーカーを実装
- client-2
  - アクセス元である`client-1`のport番号と自身のport番号を表示するメソッドを実装
- client-3
  - OpenFeign経由で`client-4`のメソッドを呼び出す
  - `client-4`呼び出し処理でサーキットブレーカーを実装
- client-4
  - アクセス元である`client-3`のport番号と自身のport番号を表示するメソッドを実装
- config-server
  - 設定ファイルを管理するconfigサーバー
- [config-repo](https://github.com/masakiii03/config-repo)(別リポジトリ)
  - 設定ファイルの一元管理
- フロントエンド
  - [microservice-frontend-sample](https://github.com/masakiii03/microservice-frontend-sample)(別リポジトリ)

## 認可コードフロー
![microservice-sequence](./microservice-sequence.svg)


## 利用方法
### 利用手順
- Github OAuth を作成して、`client_id`と`client_secret`を取得
- 取得したGithub OAuth の情報から`microservice-sample/authentication-service/src/main/resources/application.yml`, `microservice-frontend-sample/src/Login.jsx`ファイルの環境変数を指定
- サービス起動(`config-server` → `eureka-server` → その他サービスの順)
  - Eurekaサーバーとclient-1 ~ 4 は各`application.yml`のポート番号を変えて起動すればサービスの冗長化が可能
  - client-1 ~ 4 の起動時に`metadata-map.version`でバージョンを設定する
- RabbitMQの起動
  - 以下コマンドを使ってdockerでRabbitMQを起動
    - `docker run -d --hostname my-rabbit --name some-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management`
- 分散トレーシングを確認する場合
  - 以下コマンドを使ってdockerでZipkinを起動
    - `docker run -p 9411:9411 openzipkin/zipkin:latest`
- フロントエンド起動
  - [microservice-frontend-sample](https://github.com/masakiii03/microservice-frontend-sample)

## ユースケース
### サービスディスカバリ
以下アクセスで確認
- http://localhost:8761
- http://localhost:8762

### config-server
- config設定の確認
  - http://localhost:8888/{サービス名}/default
- config設定のrefresh
  - http://localhost:{対象サービスのポート番号}/actuator/refresh (POST)

### プロパティの一括refresh(Spring Cloud Bus × RabbitMQ)
- http://localhost:8080/actuator/busrefresh (POST)

### Zipkin
以下アクセスで確認
- http://localhost:9411

### 各サービス
- gateway
  - http://localhost:8080/value
    - configのvalueを取得
      - refreshの動作確認が可能
  - http://localhost:8080/client-1/sample/0
    - client-2 を呼び出すclient-1 のメソッドを呼び出す
      - gatewayのルーティングの動作確認が可能
      - カナリアリリースの動作確認が可能
  - http://localhost:8080/client-1/sample/6
    - client-1 に実装しているサーキットブレーカーの動作確認が可能
  - http://localhost:8080/client-3/sample/0
    - client-4 を呼び出すclient-5 のメソッドを呼び出す
      - gatewayのルーティングの動作確認が可能
      - カナリアリリースの動作確認が可能
  - http://localhost:8080/client-3/sample/6
    - client-3 に実装しているサーキットブレーカーの動作確認が可能

- client-1(port: 8001, 8011)
  - http://localhost:8001/value
    - configのvalueを取得
      - refreshの動作確認が可能
  - http://localhost:8001/sample/0
    - client-2 を呼び出す
  - http://localhost:8001/sample/6
    - client-2 を呼び出す
    - サーキットブレーカーの動作確認が可能
    - pathの最後に指定する数の秒数後にレスポンスが返ってくる`client-2`のメソッドを呼び出す
      - 5秒以上レスポンスが返ってこないときにサーキットブレーカーがOPENになる実装
  - http://localhost:8001/products
    - H2DBからプロダクト情報を検索する
  - http://localhost:8001/products (POST)
    - プロダクト購入処理
      - 自サービスに紐づくDBからプロダクト情報を更新、client-2 を呼出して口座情報を更新
    - 分散トランザクションの動作確認が可能

- client-2(port: 8002, 8022) ※client-1からの呼出しを前提としています。
  - http://localhost:8002/client-2/get_port/0
    - 0秒後(path指定)に自サービスのポート番号と呼び出し元(client-1)のポート番号を表示する
  - http://localhost:8002/client-2/accounts
    - H2DBから口座情報を検索する
  - http://localhost:8002/client-2/try_accounts (POST)
    - 口座情報を更新する処理のtryフェーズ
    - 分散トランザクションの動作確認が可能
  - http://localhost:8002/client-2/confirm_accounts (POST)
    - 口座情報を更新する処理のconfirmフェーズ
    - 分散トランザクションの動作確認が可能
  - http://localhost:8002/client-2/cancel_accounts (POST)
    - 口座情報を更新する処理のcancelフェーズ
    - 分散トランザクションの動作確認が可能

- client-3(port: 8003, 8033)
  - http://localhost:8003/value
    - configのvalueを取得
      - refreshの動作確認が可能
  - http://localhost:8003/sample/0
    - client-4 を呼び出す
  - http://localhost:8003/sample/6
    - client-4 を呼び出す
    - サーキットブレーカーの動作確認が可能
    - pathの最後に指定する数の秒数後にレスポンスが返ってくる`client-4`のメソッドを呼び出す
      - 5秒以上レスポンスが返ってこないときにサーキットブレーカーがOPENになる実装

- client-4(port: 8004, 8044) ※client-3からの呼出しを前提としています。
  - http://localhost:8004/client-4/get_port/0
    - 0秒後(path指定)に自サービスのポート番号と呼び出し元(client-3)のポート番号を表示する