#!/bin/bash

# チャットサーバーテストスクリプト
# Usage: ./test_chat.sh

echo "=== Chat Server Test Script ==="
echo

# サーバーが起動しているかチェック
echo "Testing server connection..."
if ! nc -z localhost 8080 2>/dev/null; then
    echo "❌ Server is not running on port 8080"
    echo "Please start the server with: java oop1.challenge.ChatServer"
    exit 1
fi

echo "✅ Server is running"
echo

# テスト1: ユーザー登録
echo "Test 1: User Registration"
echo "REGISTER TestUser" | nc localhost 8080 | head -3
echo

# テスト2: ブロードキャストメッセージ
echo "Test 2: Broadcast Message"
echo -e "REGISTER Alice\nBROADCAST Hello World\nQUIT" | nc localhost 8080 | grep -E "(REGISTERED|MESSAGE_SENT|BROADCAST_MESSAGE)"
echo

# テスト3: グループ機能
echo "Test 3: Group Functionality"
echo -e "REGISTER Bob\nJOIN #test\nGROUPCAST #test Hello Group\nQUIT" | nc localhost 8080 | grep -E "(REGISTERED|JOINED|GROUP_MESSAGE_SENT)"
echo

# テスト4: エラーハンドリング
echo "Test 4: Error Handling"
echo -e "BROADCAST NoRegister\nREGISTER\nQUIT" | nc localhost 8080 | grep "ERROR"
echo

# テスト5: ヘルプコマンド
echo "Test 5: Help Command"
echo -e "HELP\nQUIT" | nc localhost 8080 | grep "HELP" | head -3
echo

echo "=== Test Complete ==="
echo "For interactive testing, use: nc localhost 8080"
echo "For GUI client, use: java oop1.challenge.ChatClient"
