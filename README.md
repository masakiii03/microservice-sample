# microservice-sample
このリポジトリはSpringCloudのコンポーネントで構成されるマイクロサービスのサンプルです。

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

必要な依存関係
- org.springframework.cloud:spring-cloud-starter-bus-amqp

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
一部のサービスでカナリアリリースの仕組みを実装しています。

実装箇所
- client-2, client-4
  - `client-2`, `client-4`起動時にmetadata.versionをEurekaに登録する
  - `client-2`, `client-4`を呼び出す`client-1`, `client-3`でカスタムロードバランサーを実装
  - [config-repo](https://github.com/masakiii03/config-repo)の設定ファイルにある`new-version-weight`を変更してrefreshすることで、weightを変更できる
- client-1
  - `client-1`起動時にmetadata.versionをEurekaに登録する
  - `client-1`を呼び出す`gateway-service`でカスタムロードバランサーを実装
  - [config-repo](https://github.com/masakiii03/config-repo)の設定ファイルにある`new-version-weight`を変更してrefreshすることで、weightを変更できる

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
  - 通常のルーティングでなく`client-1`向けのカナリア時は、`/service/client-1/**`のパスでアクセスをする
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
  - Eurekaサーバーとclient-1 ~ 4は各`application.yml`のポートを変えて起動すればサービスの冗長化が可能
- RabbitMQの起動
  - 以下コマンドを使ってdockerでRabbitMQを起動
    - `docker run -d --hostname my-rabbit --name some-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management`
- 分散トレーシングを確認する場合
  - 以下コマンドを使ってdockerでZipkinを起動
    - `docker run -p 9411:9411 openzipkin/zipkin:latest`
- フロントエンド起動
  - [microservice-frontend-sample](https://github.com/masakiii03/microservice-frontend-sample)

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
- http://localhost:9411