### 1. 如何修改倍速选项

为了修改倍速选项，需要修改以下两个文件，它们分别负责横屏和竖屏模式下的倍速选择UI：

- PLVMediaPlayerSpeedSelectLayoutPortrait.kt (竖屏模式下的倍速选择布局)
- PLVMediaPlayerSpeedSelectLayoutLandscape.kt (横屏模式下的倍速选择布局)

为了保证在两种屏幕方向下都能看到倍速选项，这两个文件都需要进行相应的修改。以下示例如何添加2.5倍速：

在这两个文件中，倍速选项是由一个名为 `SUPPORT_SPEED_LIST` 的`listOf<String>`常量定义的。您只需要将 `"2.5"` 添加到这个列表中即可。

#### 步骤说明：

1. **打开 `PLVMediaPlayerSpeedSelectLayoutPortrait.kt` 文件。**
2. **找到 `SUPPORT_SPEED_LIST` 定义。** 它通常位于文件的顶部，如下所示：

   ```kotlin
   // PLVMediaPlayerSpeedSelectLayoutPortrait.kt
   // ...
   private val SUPPORT_SPEED_LIST = listOf("0.5", "0.75", "1", "1.25", "1.5", "2", "3")
   // ...
   ```

3. **修改 `SUPPORT_SPEED_LIST`，将 `"2.5"` 添加到列表中。** 建议将其放在 `"2"` 和 `"3"` 之间，以保持倍速的顺序性。

   ```diff
   --- a/PLVMediaPlayerSpeedSelectLayoutPortrait.kt
   +++ b/PLVMediaPlayerSpeedSelectLayoutPortrait.kt
   
   -private val SUPPORT_SPEED_LIST = listOf("0.5", "0.75", "1", "1.25", "1.5", "2", "3")
   +private val SUPPORT_SPEED_LIST = listOf("0.5", "0.75", "1", "1.25", "1.5", "2", "2.5", "3")
    private val TEXT_COLOR_SELECTED = parseColor("#3F76FC")
    private val TEXT_COLOR_NORMAL = parseColor("#333333")
   // ...
   ```

4. **对 `PLVMediaPlayerSpeedSelectLayoutLandscape.kt` 文件重复上述步骤。**
   同样，找到其文件中的 `SUPPORT_SPEED_LIST` 并进行修改：

   ```diff
   --- a/PLVMediaPlayerSpeedSelectLayoutLandscape.kt
   +++ b/PLVMediaPlayerSpeedSelectLayoutLandscape.kt
   
   -private val SUPPORT_SPEED_LIST = listOf("0.5", "0.75", "1", "1.25", "1.5", "2", "3")
   +private val SUPPORT_SPEED_LIST = listOf("0.5", "0.75", "1", "1.25", "1.5", "2", "2.5", "3")
    private val TEXT_COLOR_SELECTED = parseColor("#3F76FC")
    private val TEXT_COLOR_NORMAL = Color.WHITE
   // ...
   ```

完成这两个文件的修改后，重新编译并运行您的应用，您将在播放器的倍速选择界面中看到2.5倍速选项。
