# チャットサーバー実装

## 概要

Java Socket 通信を使用したチャットサーバーとクライアントの実装です。
設計書「K24083_10_normal.md」の仕様に完全準拠しています。

## ファイル構成

- `ChatServer.java` - チャットサーバー本体
- `ChatClient.java` - チャットクライアント
- `README.md` - このファイル

## 使用方法

### 1. コンパイル

````bash
cd /Users/k24083kk/development/java/src/10/oop1/challenge
javac ChatServer.java
javac ChatClient.java
```java oop1.challenge.ChatServer

### 2. サーバー起動

```bash
java oop1.challenge.ChatServer
````

サーバーが起動すると以下のメッセージが表示されます：

```
Chat Server started on port 8080
Use 'nc localhost 8080' to connect or run ChatClient
```

### 3. クライアント接続

#### Java クライアントを使用する場合

```bash
# 新しいターミナルで実行
java oop1.challenge.ChatClient
```

#### nc コマンドを使用する場合

```bash
# 新しいターミナルで実行
nc localhost 8080
```

## 使用例

### 基本的なチャットの流れ

1. **ユーザー登録**

   ```
   > REGISTER Alice
   ✓ REGISTERED Alice
   ```

2. **全体メッセージ送信**

   ```
   > BROADCAST Hello everyone!
   ✓ MESSAGE_SENT
   [BROADCAST] Alice Hello everyone!
   ```

3. **グループ参加**

   ```
   > JOIN #tech
   ✓ JOINED #tech
   ```

4. **グループメッセージ送信**

   ```
   > GROUPCAST #tech Let's discuss the new feature
   ✓ GROUP_MESSAGE_SENT
   [GROUP] #tech Alice Let's discuss the new feature
   ```

5. **状態確認**

   ```
   > STATUS
   100 INFO STATUS Connected as: Alice
   100 INFO STATUS Online users: [Alice, Bob]
   100 INFO STATUS Joined groups: [#tech]
   ```

6. **終了**
   ```
   > QUIT
   ✓ GOODBYE
   Disconnected from server.
   ```

### nc コマンドでの簡単テスト

```bash
# 基本コマンドテスト
echo "REGISTER TestUser" | nc localhost 8080
echo -e "REGISTER Bob\nBROADCAST Hello\nQUIT" | nc localhost 8080

# 対話的テスト
nc localhost 8080
```

## サポートするコマンド

| コマンド  | 形式                               | 説明                     |
| --------- | ---------------------------------- | ------------------------ |
| REGISTER  | `REGISTER <username>`              | ユーザー名を登録         |
| BROADCAST | `BROADCAST <message>`              | 全体メッセージを送信     |
| JOIN      | `JOIN <group_name>`                | グループに参加           |
| LEAVE     | `LEAVE <group_name>`               | グループから退出         |
| GROUPCAST | `GROUPCAST <group_name> <message>` | グループメッセージを送信 |
| STATUS    | `STATUS`                           | 現在の状態を表示         |
| HELP      | `HELP`                             | コマンド一覧を表示       |
| QUIT      | `QUIT`                             | サーバーから切断         |

## 機能

- ✅ マルチユーザー対応（同時接続）
- ✅ ユーザー名登録・管理
- ✅ 全体メッセージ配信
- ✅ グループチャット機能
- ✅ リアルタイムメッセージ配信
- ✅ エラーハンドリング
- ✅ 状態管理・表示
- ✅ nc コマンド対応
- ✅ 対話的プロンプト表示

## 技術仕様

- **Java Version**: Java 8 以上
- **通信プロトコル**: TCP Socket
- **文字エンコーディング**: UTF-8
- **ポート番号**: 8080
- **スレッドモデル**: ExecutorService による並行処理
- **データ構造**: ConcurrentHashMap によるスレッドセーフな状態管理

## 注意事項

- サーバーを停止する場合は `Ctrl+C` を使用してください
- 複数のクライアントを同時に接続してテストできます
- ユーザー名は英数字とアンダースコアのみ、最大 20 文字です
- グループ名に制限はありませんが、#を付けることを推奨します
