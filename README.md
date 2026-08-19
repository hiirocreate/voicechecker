# 声域アナライザー (Voice Range Analyzer)

Android 12 (API 31) 〜 最新バージョンで動作する、個人の練習用ボイストレーニング支援アプリです。
マイクに向かって発声するだけで、以下を測定・分析します。

- **声域測定**: 最低音〜最高音(音名付き)、音域の広さ(オクターブ表示)
- **音域ごとの声量プロファイル**: どの高さで声量が出やすい/出にくいかを可視化し、「得意な音域」「苦手な音域」を自動判定
- **おすすめ音域**: 得意な音域(歌うと良い曲の目安)、苦手な音域(練習すると効果的な目安)を提示
- **声種の目安**: 測定した音域から、バス/バリトン/テノール/アルト/メゾソプラノ/ソプラノに最も近いものを推定
- **平均音域との比較**: 一般的な男性/女性の歌唱音域の目安に対して、最低音・最高音がどれだけ違うかを半音単位で表示
- **ビブラート・音程安定性測定**: 一定の高さで声を伸ばし、ビブラートの有無・速さ・深さ、音程のジッター(揺れ)を分析
- **測定履歴**: 端末内のRoom DBに保存し、過去の測定結果を後から見返せる

すべてのデータは端末内(ローカルDB)に保存され、外部には一切送信されません。

## 精度についての重要な注意

- スマートフォン内蔵マイクは機種ごとに感度が異なるため、**絶対的な音圧レベル(dB SPL)は測定できません**。
  「声量」は自分自身の発声の中での相対的な強弱の指標(dBFS相当)としてご利用ください。
- ビブラート・ジッター分析は、業務用のボイス解析機器のような厳密な音響解析ではなく、
  ピッチトラッキング結果から推定する簡易的な指標です。
- 「声種の目安」「平均音域との比較」は、声楽で一般的に使われる音域区分をもとにした
  簡易的な参考値であり、声質(声の質感)や訓練度などは考慮していません。あくまで自己比較の目安です。
- 本アプリは医療機器・診断用ツールではありません。

## 動作環境

- Android 12 (API 31) 〜 最新版
- マイク付き端末必須
- 個人利用を想定した構成(Google Play向けの署名・審査対応などは含みません)

## 技術構成

- Kotlin + Jetpack Compose (Material 3)
- ピッチ検出: YINアルゴリズムを純Kotlinで実装(外部ネイティブライブラリ不使用)
- Room (SQLite) による測定履歴の永続化
- Jetpack Navigation Compose
- 最小限の依存関係のみを使用し、ビルドの安定性を優先しています

## APKの入手方法(GitHub Actions)

このプロジェクトには `.github/workflows/android-build.yml` が同梱されており、
GitHubにpushするだけで自動的にAPKがビルドされます。

1. GitHub上で新しいリポジトリを作成する(Public/Privateどちらでも可)
2. このプロジェクト一式をpushする

   ```bash
   cd VoiceRangeAnalyzer
   git remote add origin https://github.com/<あなたのユーザー名>/<リポジトリ名>.git
   git push -u origin main
   ```

   (このフォルダは既に `git init` 済み、初回コミットも作成済みです)

3. GitHubの対象リポジトリの **Actions** タブを開く
4. 「Android CI Build」ワークフローの実行が完了すると、**Artifacts** に
   `voice-range-analyzer-debug-apk` が生成されるのでダウンロードする(zip形式)
5. zipを展開すると `app-debug.apk` が入っているので、Android端末に転送してインストールする

### タグを打つとGitHub Releaseにも自動添付されます

```bash
git tag v1.0.0
git push origin v1.0.0
```

とすると、Releaseページに直接APKが添付されます。

## 端末へのインストール方法(サイドロード)

Google Playを経由しないため、初回のみ「提供元不明のアプリ」のインストールを許可する必要があります。

1. APKファイルを端末にダウンロード(Google Driveやメール、USB転送など)
2. ダウンロードしたAPKをタップ
3. 「このソースからのアプリを許可」を求められたら許可する(端末のブラウザ/ファイルアプリごとに設定)
4. インストール完了後、通常のアプリと同様に起動できます

## Android Studioで開く場合

GitHub Actionsを使わず、自分のPCで直接ビルド・実行することも可能です。

1. Android Studio (Giraffe以降推奨) をインストール
2. このフォルダを「Open」で開く
3. Gradle Syncが完了したら、実機またはエミュレータ(API 31以上)で実行

## プロジェクト構成

```
app/src/main/java/com/vocalrange/analyzer/
├── audio/       # マイク録音(AudioRecord)、YINピッチ検出
├── core/        # 音名変換、声量計算、声域/ビブラート解析ロジック(Android非依存)
├── data/        # Room DB、Entity、Repository
├── ui/          # Jetpack Compose画面、ViewModel、Navigation
```

## カスタマイズしたい場合の主なパラメータ

- `RangeAnalyzer` 内の `STRONG_OFFSET_DB` / `WEAK_OFFSET_DB`: 得意/苦手音域の判定基準(dB差)
- `VibratoAnalyzer` 内の `MIN_VIBRATO_RATE_HZ` / `MAX_VIBRATO_RATE_HZ`: ビブラートとみなす速さの範囲
- `AverageRanges`: 比較対象とする平均音域の参照値
- `PitchTracker` の `silenceThresholdDb`: 無音とみなす音量のしきい値

## 既知の制限・今後の拡張案

- 声質(ハスキーさ、明るさなど)の分析は未対応
- 曲名ベースのレコメンドは行わず、音域(音名の範囲)のみで提示
- 複数端末間でのデータ同期は未対応(端末内保存のみ)
