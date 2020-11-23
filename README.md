AndroidTUTCode
==============

[SKK for Android 3.4](http://ray-mizuki.la.coocan.jp/software/skk_jp.html)
をベースに、TUT-Code用の変更をしたものです。

ハードウェアキーボードでの使用のみ想定しています。
(ソフトウェアキーボードは動作未確認)

## 主な変更点

* ローマ字かな変換表をTUT-Codeに変更。
  * ローマ字かな変換表(漢字表)をファイルからインポートする機能を追加。
    T-Code等も使えるように。
* 交ぜ書き変換の読み入力開始を、SKK流の大文字でなく、`alj`に変更。
  インポートする漢字表により、他のシーケンスに変更可能。
* 辞書を交ぜ書き変換辞書におきかえ。
* ASCIIモードに移行するキーと設定を追加。
  かなキーのトグル動作だと、現在のモードの意識が必要で面倒なので。

### 無効化した機能

* Abbrevモード
* 候補絞り込み・接頭辞接尾辞変換

## ローマ字かな変換表(漢字表)のインポート機能
デフォルトのTUT-Code以外の、T-Code等を使いたい場合用の機能です。

* 各行に、タブ区切りで入力シーケンス(ローマ字)と対応する漢字を記述。
* UTF-8。BOMは付けないでください。
* インポートすると内部ストレージにコピーするので、
  元ファイルを変更した場合は、再度インポートしてください。

## ライセンス

[LICENSE](./LICENSE) Apache ライセンスが適用されます。

## オリジナル著作者

* 海月玲二 さん
* minghai さん
