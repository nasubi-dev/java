# Clipper 仕様書

## 背景

開発作業において、頻繁にコピー＆ペーストを行うが、過去のクリップボード履歴にアクセスするのが困難な状況が多発していた｡MacOSにはRaycastやPastebotなどの多機能クリップボードアプリが存在するが、使い勝手や機能面で私が満足できるものがないという問題が発生していた｡

## 目的

Java を使用して、使いやすく機能的なクリップボード管理アプリケーションを自作し、開発効率を向上させる。

## 機能説明

Clipper は、クリップボードの履歴を管理し、ユーザーが過去にコピーしたテキストを簡単に再利用できるようにするアプリケーションです。

### 基本機能

- 自動クリップボード監視: Command + C を検出し、自動的にテキストを Java アプリで保存
- ワンクリック再コピー: 保存済みのテキスト要素をクリックして、再度クリップボードにコピー
- 履歴管理: 不要なテキストエントリの削除機能
- 日別分類: 左サイドバーで日付別にクリップボード履歴を整理
- お気に入り機能: よく使うテキストをピン留め

## 画面構成説明

### メインウィンドウ

- 左サイドバー（20%）: 日付別ナビゲーション
  - 今日、昨日、一昨日...の階層表示
  - 各日付の保存件数表示
- 右メインエリア（80%）: クリップボード履歴表示
  - リスト形式でのテキスト表示
  - タイムスタンプ付きエントリ
  - テキストプレビュー（最初の 2-3 行）
  - 各エントリの操作ボタン（コピー、削除、お気に入り）

### UI コンポーネント

- ツールバー: 検索バー、設定ボタン、クリア機能
- ステータスバー: 現在の監視状態、総保存件数
- コンテキストメニュー: 右クリックでの詳細操作

## データの取り扱い

### データ構造

```java
ClipboardEntry {
    String id;              // 一意識別子
    String text;            // コピーされたテキスト
    LocalDateTime timestamp; // コピー時刻
    String source;          // コピー元アプリ（取得可能な場合）
    boolean isFavorite;     // お気に入りフラグ
}
```

### データ保存方式

- ファイル形式: CSV 形式でローカルファイルに保存
- 保存場所: `~/Documents/ClipperData/`
- ファイル分割: 日付別にファイルを分割（例：`2025-07-15.csv`）
- データ形式: `id,timestamp,isFavorite,category,text`（テキストは適切にエスケープ）

### データ管理

- 読み込み: アプリ起動時に最新のデータを読み込み
- 保存: 新しいクリップボードエントリが追加されるたびに自動保存
- 削除: ユーザーが不要なエントリを削除可能

## 技術仕様

### 開発環境

- 言語: Java 21+
- GUI フレームワーク: Java Swing
  - Java 標準の GUI ライブラリを使用してクロスプラットフォーム対応
  - レスポンシブなユーザーインターフェースを実現
- データ処理: Java 標準ライブラリ（CSV）
- ビルドツール: Maven

### プロジェクト構造

```
src/
└── Clipper/
    ├── ClipperApp.java          // メインクラス（アプリケーション起動）
    ├── model/
    │   ├── ClipboardEntry.java  // データモデルクラス
    │   └── ClipboardData.java   // データ管理クラス
    ├── gui/
    │   ├── MainWindow.java      // メインウィンドウ
    │   ├── SideBarPanel.java    // 左サイドバー
    │   ├── HistoryPanel.java    // 履歴表示パネル
    │   └── EntryPanel.java      // 個別エントリ表示
    ├── service/
    │   ├── ClipboardMonitor.java // クリップボード監視
    │   └── FileManager.java     // ファイル読み書き
    └── util/
        ├── DateUtil.java        // 日付ユーティリティ
        └── CsvUtil.java         // CSV処理ユーティリティ
```
### システム要件

- OS: macOS 10.14+
- Java: JRE 17 以上

### 実装時の注意点

- すべての GUI 操作は EDT（Event Dispatch Thread）で実行
- SwingUtilities.invokeLater() を適切に使用
- 長時間処理は別スレッドで実行し、結果のみ EDT で更新
- ディレクトリが存在しない場合は自動作成
- CSV の特殊文字（カンマ、改行、ダブルクォート）は適切にエスケープ
- ファイル読み書き時の例外処理を必ず実装
- 大量データ表示時はページネーションまたは仮想化を検討
- 検索時は部分一致でリアルタイムフィルタリング
- ファイル保存は非同期で実行
- クリップボードアクセス失敗時の処理
- ファイル読み書き失敗時の処理
- 不正な CSV データの処理

## 制約事項

- Java の制限により、完全なリアルタイム監視は困難（ポーリング方式を採用）
- macOS 特有のセキュリティ制限への対応が必要
- 大量データ保存時のパフォーマンス考慮が必要
