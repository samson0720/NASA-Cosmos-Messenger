# NASA Cosmos Messenger

[![Android CI](https://github.com/samson0720/NASA-Cosmos-Messenger/actions/workflows/android-ci.yml/badge.svg)](https://github.com/samson0720/NASA-Cosmos-Messenger/actions/workflows/android-ci.yml)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose)
![Android](https://img.shields.io/badge/Android-SDK%2034-3DDC84?logo=android)

一個以對話形式瀏覽 NASA Astronomy Picture of the Day（APOD）的 Android App，使用者可以輸入日期查詢 APOD、收藏喜歡的天文圖片、離線回看或快速取得已查詢過的 APOD，並把 APOD 轉成看圖解說、生日星空卡或 3 張收藏圖片的回憶拼貼。

本專案使用 Kotlin、Jetpack Compose、Retrofit、Moshi、Room 與 Flask + Groq LLM gateway 實作，重點放在 Android-native 體驗、清楚資料分層、穩定錯誤處理、API key 安全邊界。

## App Preview

<table>
  <tr>
    <td align="center" width="50%"><img src="./docs/readme-assets/nova-chat.png" width="320" alt="Nova chat image APOD" /></td>
    <td align="center" width="50%"><img src="./docs/readme-assets/video-apod.png" width="320" alt="Video APOD handling" /></td>
  </tr>
  <tr>
    <td align="center"><b>Nova 日期查詢</b><br />輸入日期後以聊天卡片顯示 APOD。</td>
    <td align="center"><b>Video APOD</b><br />影片類型改以外部連結開啟。</td>
  </tr>
  <tr>
    <td align="center" width="50%"><img src="./docs/readme-assets/favorites-grid.png" width="320" alt="Favorites grid" /></td>
    <td align="center" width="50%"><img src="./docs/readme-assets/offline-cache.png" width="320" alt="Offline cache APOD card" /></td>
  </tr>
  <tr>
    <td align="center"><b>本機收藏</b><br />收藏 APOD 並在 Favorites 頁管理。</td>
    <td align="center"><b>Offline cache</b><br />APOD 成功查詢後寫入 Room cache，同日期再次查詢可直接回覆。</td>
  </tr>
</table>

## Bonus Features

<table>
  <tr>
    <td align="center" width="50%"><img src="./docs/readme-assets/nova-guide.png" width="320" alt="Nova Guide APOD explanation" /></td>
    <td align="center" width="50%"><img src="./docs/readme-assets/star-card-preview.png" width="320" alt="Birthday star card preview" /></td>
  </tr>
  <tr>
    <td align="center"><b>看圖解說</b><br />把 NASA 原文轉成繁中白話說明，並保留原文切換。</td>
    <td align="center"><b>生日星空卡預覽</b><br />可從 Nova APOD 或 Favorites 收藏項目生成預覽。</td>
  </tr>
  <tr>
    <td align="center" width="50%"><img src="./docs/readme-assets/star-card-result.jpg" width="320" alt="Generated birthday star card" /></td>
    <td align="center" width="50%"><img src="./docs/readme-assets/collage-polaroid-result.jpg" width="320" alt="Generated APOD polaroid collage" /></td>
  </tr>
  <tr>
    <td align="center"><b>星空卡成品</b><br />生成可透過 Android share sheet 分享的圖片。</td>
    <td align="center"><b>收藏拼貼</b><br />從收藏中選 3 張 image APOD 合成回憶拼貼。</td>
  </tr>
</table>

## Favorite Collage Templates

收藏拼貼提供三種可選模板，使用者從收藏中選滿 3 張 image APOD 後，可以選擇其中一種生成分享圖片。

<table>
  <tr>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/collage_template_polaroid_orbit.jpg" width="300" alt="Polaroid Orbit template" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/collage_template_mission_board.jpg" width="300" alt="APOD Mission Board template" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/collage_template_celestial_journal.jpg" width="300" alt="Celestial Journal template" /></td>
  </tr>
  <tr>
    <td align="center">星軌拼貼</td>
    <td align="center">APOD 任務板</td>
    <td align="center">星象手帳</td>
  </tr>
</table>

## Birthday Star Card Templates

生日星空卡會依照使用者生日對應星座，套用對應模板生成分享圖片。

<table>
  <tr>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_aries.jpg" width="180" alt="Aries star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_taurus.jpg" width="180" alt="Taurus star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_gemini.jpg" width="180" alt="Gemini star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_cancer.jpg" width="180" alt="Cancer star card" /></td>
  </tr>
  <tr>
    <td align="center">Aries</td>
    <td align="center">Taurus</td>
    <td align="center">Gemini</td>
    <td align="center">Cancer</td>
  </tr>
  <tr>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_leo.jpg" width="180" alt="Leo star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_virgo.jpg" width="180" alt="Virgo star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_libra.jpg" width="180" alt="Libra star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_scorpio.jpg" width="180" alt="Scorpio star card" /></td>
  </tr>
  <tr>
    <td align="center">Leo</td>
    <td align="center">Virgo</td>
    <td align="center">Libra</td>
    <td align="center">Scorpio</td>
  </tr>
  <tr>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_sagittarius.jpg" width="180" alt="Sagittarius star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_capricorn.jpg" width="180" alt="Capricorn star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_aquarius.jpg" width="180" alt="Aquarius star card" /></td>
    <td align="center"><img src="./app/src/main/res/drawable-nodpi/starcard_template_pisces.jpg" width="180" alt="Pisces star card" /></td>
  </tr>
  <tr>
    <td align="center">Sagittarius</td>
    <td align="center">Capricorn</td>
    <td align="center">Aquarius</td>
    <td align="center">Pisces</td>
  </tr>
</table>

## Requirement Coverage

| Core requirement | 說明 |
| --- | --- |
| 兩個主要分頁 | Nova 對話頁、Favorites 收藏頁 |
| Chat-style APOD | 使用者訊息靠右、Nova 回覆靠左，新訊息送出或回覆後自動捲到底部 |
| 長按加入收藏 | 在 Nova 對話中長按 APOD 圖片訊息即可加入收藏 |
| 日期查詢 | 支援 `yyyy/MM/dd`、`yyyy-MM-dd`、`yyyy.MM.dd`，也可輸入包含日期的句子；APOD 最早日期為 1995-06-16 |
| NASA APOD API | Retrofit + Moshi，支援今日與指定日期查詢 |
| Image / Video handling | image 顯示卡片，video 顯示外部開啟按鈕 |
| Favorites | Room 本機保存、瀏覽、刪除、開啟來源 |
| README / demo 支援 | README 說明架構、日期格式、bonus 完成項目，並提供 demo video 連結 |

## Bonus Features Implemented

| Bonus feature | 說明 |
| --- | --- |
| Offline cache | 成功查詢後寫入 Room / SQLite cache；同日期再次查詢時先回傳本機資料，網路失敗時也能依日期 fallback |
| Birthday star card | 點擊 APOD 後將圖片、標題、日期與星座模板合成生日星空卡，並用系統分享 |
| Nova Guide | 圖片 APOD 可產生繁中白話解說，並保留 NASA 原文 |
| Favorite collage | 從收藏選 3 張 image APOD，選模板，預覽並分享 |

## Architecture Choices

| Layer | 選擇與原因 |
| --- | --- |
| UI | Jetpack Compose + Material 3，適合快速建立 Android-native 的 chat 與 grid 介面 |
| State | ViewModel + StateFlow，讓畫面狀態可測試，並避免 Composable 直接處理 business logic |
| Data | Repository 包住 Retrofit 與 Room，UI 不直接碰 API / SQL 細節 |
| Local storage | Room / SQLite 儲存 favorites 與 APOD cache，資料用途清楚分離 |
| Image loading | Coil 負責 APOD 圖片載入與快取，避免手寫 bitmap loading |
| AI Guide | Groq API key 留在 Flask backend，Android 只保存 endpoint，降低 key 外洩風險 |

## Quality

| 項目 | 說明 |
| --- | --- |
| Unit tests | 80 個 JVM unit tests，覆蓋日期解析、repository、mapper、Room DAO 與 ViewModel selection 規則 |
| GitHub Actions | CI 會執行 unit tests、build debug APK，並上傳 debug APK artifact |
| API resilience | 已查詢過的日期會先讀 Room cache；cache miss 才呼叫 NASA APOD API，timeout 或暫時失敗時會 retry |
| Data separation | favorites 與 APOD cache 使用不同 Room table，避免不同資料生命週期互相污染 |

## Local Data

本機資料使用 Room / SQLite 管理，依照用途拆成獨立資料表，避免把收藏與快取混在同一個資料模型。

| Room table | 用途 |
| --- | --- |
| `favorite_apod` | 保存使用者明確收藏的 APOD，可瀏覽、刪除與開啟來源 |
| `cached_apod` | 保存成功查詢過的 APOD，同日期再次查詢時直接回覆，cache miss 才呼叫 NASA API |

## External API Reliability

NASA APOD API 與 Nova Guide backend 都是外部服務，實際使用時可能遇到回應較慢、timeout、暫時連不上或 server error。App 針對這些狀況做了防護：

- 同日期 APOD 已存在本機 cache 時會直接回覆，避免重複等待外部 API。
- cache miss 後才呼叫 NASA APOD API，遇到 transient error 時會重試一次。
- 網路失敗時會依日期讀取 Room / SQLite offline cache。
- cache 命中時會清楚標記 `Offline cache`，避免使用者誤以為是最新網路結果。
- Nova Guide endpoint 未設定或暫時無法使用時，不會影響 APOD 查詢、收藏、星空卡與收藏拼貼功能。

## Tech Stack

| 技術 | 用途 |
| --- | --- |
| Kotlin / Coroutines | Android app 與非同步流程 |
| Jetpack Compose / Material 3 | UI |
| ViewModel / StateFlow | 狀態管理 |
| Retrofit / Moshi / OkHttp | NASA APOD API 與 Nova Guide backend |
| Room | Favorites 與 APOD cache |
| Coil | APOD image loading |
| JUnit / Robolectric / AndroidX Test Core | JVM unit tests |
| GitHub Actions | CI、unit tests、debug APK artifact |

## Run Locally

### API Keys

Android app 會從 `local.properties` 讀取 NASA API key 與選填的 Nova Guide endpoint：

```properties
NASA_API_KEY=your_key_here
NOVA_GUIDE_ENDPOINT=http://10.0.2.2:5050/v1/apod-guide
```

如果沒有設定 `NASA_API_KEY`，App 會 fallback 到 NASA `DEMO_KEY`，讓 fresh clone 後仍可 build/run。`NOVA_GUIDE_ENDPOINT` 是選填；未設定時 APOD 查詢、Favorites、生日星空卡與收藏拼貼仍可正常使用。

### Build And Test

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project Links

- [Notion Kanban](https://www.notion.so/347315bb151b8088a834e39b7f4ca209?v=347315bb151b8014b4db000c452f293f)
- [Demo video](https://youtu.be/OB3Bx7gs-iE?si=Tt5XeXULb63X-uPm)
