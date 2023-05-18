# microservice-sample
このリポジトリはSpringCloudのコンポーネントで構成されるマイクロサービスのサンプルです。

## コンポーネント

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

## システム構成
![microservice](./microservice.drawio.svg)

- Eureka Server
  - サービスディスカバリー
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