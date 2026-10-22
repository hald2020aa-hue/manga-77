# تقرير الهندسة العكسية والتحليل الثابت

**الملف الذي جرى فحصه:** قارئ مانجا 2.0.7.6.11 — APK داخل ZIP مرفق.

**نوع التحليل:** فحص الأرشيف وبيانات AndroidManifest.xml ومؤشرات bytecode (DEX) وملفات الموارد. **لم يُفك الكود إلى مصدر كامل، ولم تُجرَ اختبارات تشغيل أو تجميع للأصل**.

## هوية الحزمة

- package: `org.hamak.mangareader`
- versionName: `2.0.7.6.11`، versionCode: `276`
- minSdk: `25`، targetSdk: `36`
- اسم Application من البيان: `com.pairip.application.Application`، مما يدل على وجود مكوّن حماية/ترخيص PairIP؛ أي محاولة تعديل APK الأصلي وإعادة توقيعه قد تؤدي لتعطل وظائفه.
- الحجم بعد فك ZIP: 23730235 بايت.
- بصمة SHA-256: `7cf3be41439d62163d15caa4a75ac84aa2fc300b2f7764baca671fa69afbb0f6`.

## وحدات DEX

| الوحدة | تعريفات أصناف | عدد سلاسل DEX | الحجم بالبايت |
|---|---:|---:|---:|
| `classes.dex` | 11,615 | 68,767 | 9,902,660 |
| `classes2.dex` | 38 | 755 | 46,636 |
| `classes3.dex` | 150 | 2,068 | 284,072 |
| `classes4.dex` | 7,418 | 42,944 | 7,402,652 |
| `classes5.dex` | 2,802 | 15,201 | 2,862,856 |

**إجمالي تعريفات الأصناف:** 22,023؛ **ضمن مساحة أسماء التطبيق الأساسية:** 2,425.

## وظائف استُدل عليها من أسماء الفئات والموارد

- **القارئ/إعدادات القراءة** — شاهد: `org.hamak.mangareader.feature.preview.CatalogGenreNavigator$open$1`.
- **المصادر ومزودو المواقع** — شاهد: `org.hamak.mangareader.providers.FavouritesProvider$add$1`.
- **المكتبة وواجهة التطبيق** — شاهد: `org.hamak.mangareader.feature.main.BillingActivity$checkServerThenPurchase$1`.
- **التنزيلات** — شاهد: `org.hamak.mangareader.feature.download.BaseViewModel$launchLoadingJob$1`.
- **النسخ الاحتياطي والتزامن** — شاهد: `org.hamak.mangareader.feature.settings.backup.BackupCreationStage`.
- **الحسابات والسحابة** — شاهد: `org.hamak.mangareader.feature.cloud.account.AccountDeletionRepository$call$1`.
- **البحث** — شاهد: `org.hamak.mangareader.feature.search.SearchActivity2$observe$1$1$1`.
- **الفصول الجديدة والإشعارات** — شاهد: `org.hamak.mangareader.feature.newchapter.NewChaptersActivity`.

## شبكات الإعلانات والحماية

- كود DEX يتضمن أسماء/واجهات مرتبطة بـ **Unity Ads، AppLovin، Vungle** وغيرها. وجود المكتبات لا يثبت أن كل شبكة فعالة أثناء الاستخدام.
- البيان يطلب `com.google.android.gms.permission.AD_ID` و `android.permission.ACCESS_ADSERVICES_AD_ID` وصلاحيات Topics/Attribution، كما يطلب `com.android.vending.BILLING` و`com.android.vending.CHECK_LICENSE`.
- وُجدت مؤشرات مثل `this_work_has_blocked` و `AdultBlocked` و `blocked_users`، **لكن لم يثبت من فحص السلاسل وحده وجود قائمة محددة بحظر روابط/أعمال**. الحظر قد يكون قواعد محلية أو حالة من الخادم أو تفضيلات المستخدم.
- يحتوي الأصل على اختبارات لتطبيع أسماء مضيفي المصادر، ومنها أمثلة ترفض عناوين IP المحلية وبعض أشكال أسماء المضيف. هذا مؤشر لبنية اكتشاف المصادر، وليس دليلًا كافيًا على حظر أعمال بعينها.

## خطة النسخة المستقلة المرفقة

1. تطبيق جديد بمعرّف مختلف `com.mangaxw.clean`؛ **لا يغيّر التوقيع أو الاشتراكات أو السحابة الخاصة بالتطبيق الأصلي**.
2. إضافة أي عنوان HTTP أو HTTPS صالح دون قائمة أسماء محظورة محلية (عدا البروتوكولات غير الآمنة مثل `javascript:` و`file:`).
3. عدم تضمين أي مكتبات SDK لإعلانات أو مشتريات؛ فلترة نطاقات الإعلانات المعروفة في WebView. **قد تظهر إعلانات مدمجة من نفس خادم الموقع**.
4. إدارة مصادر ومكتبة وسجل، عرض ويب، محاولة استخراج صور الصفحات، قراءة عمودية/أفقية، تنزيل صور للعمل بلا اتصال، نسخ احتياطي JSON للبيانات الوصفية.
5. **ما زال ناقصًا قياسًا بالأصل:** موفّرات جميع المواقع المخصصة، المزامنة السحابية، المجتمع، نقل قواعد بيانات التطبيق الأصلي، إعدادات القراءة المتقدمة، قارئ ZIP/CBZ، والتعرف المؤكد على جميع الصفحات في كل موقع.

## حدّ الاختبار

تم فحص صياغة ملفات Java وفحص سلامة XML؛ **لم ينتج APK من المشروع البديل، لأن Android SDK وGradle غير متوفرين في بيئة التحليل هذه**. مُعدّ مشروع Gradle ومسار بناء آلي عبر GitHub Actions للاختبار الحقيقي في بيئة Android.
