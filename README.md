# DrawableImageView



`DrawableImageView`是`ImageView`的一个扩展类, 有编辑模式和非编辑模式. 在编辑模式下, 可以在图片上进行涂鸦操作(包括画轨迹, 画直线和擦除操作), 可以进行撤销和恢复; 在非编辑模式下, 可以将其视为`ImageView`.



## 使用

像`ImageView`一样添加到布局文件和实例化即可.



### 设置编辑模式

```java
public void toggleEditMode() { // 切换编辑模式和非编辑模式
    if (mDrawableImageView.isInEditMode()) { //判断是不是在编辑模式下
		mDrawableImageView.setInEditMode(false); // 设置为非编辑模式
	} else {
		mDrawableImageView.setInEditMode(true); // 设置为编辑模式
  	}
  	refreshButtonStatus();
}
```

### 设置涂鸦方式

```java
// 在编辑模式下，设置涂鸦方式为涂画
if (mDrawableImageView.isInEditMode()) { 
	mDrawableImageView.setDrawingMode(DrawOperation.DrawingMode.DRAW);
}
// 在编辑模式下，设置涂鸦方式为擦除
if (mDrawableImageView.isInEditMode()) {
	mDrawableImageView.setDrawingMode(DrawOperation.DrawingMode.ERASE);
}
```

### 设置画笔类型

```java
mDrawableImageView.setPenType(DrawOperation.PenType.NORMAL); // 设置画笔类型为普通，即画轨迹
mDrawableImageView.setPenType(DrawOperation.PenType.LINE); 	// 设置画笔类型为画线
mDrawableImageView.setPenType(DrawOperation.PenType.POINT); // 设置画笔类型为画点
```

### 设置笔触大小，颜色

```java
mDrawableImageView.setPenWidth(10); // 设置笔触大小为10
mDrawableImageView.setPenColor(Color.RED); // 设置笔触颜色为红色
```

### 执行撤销操作, 恢复操作, 和清空操作

```java
// 在编辑模式下，撤销一一次涂鸦操作
if (mDrawableImageView.isInEditMode()) {
	mDrawableImageView.undo();
}
// 在编辑模式下，清空所有涂鸦
if (mDrawableImageView.isInEditMode()) {
	mDrawableImageView.redo();
}
// 在编辑模式下，清空所有涂鸦
if (mDrawableImageView.isInEditMode()) {
	mDrawableImageView.clearAll();
}
// 查询是否可以进行撤销操作
if (mDrawableImageView.canUndo()) {
  // ...
}
// 查询是否可以进行恢复操作
if (mDrawableImageView.canRedo()) {
  // ...
}
```

### 获取当前涂鸦快照

```java
mDrawableImageView.createCaptureBytes() // 以字节数组的形式返回图像
mDrawableImageView.createCaptureBitmap() // 以Bitmap的形式返回图像
```

### 修改涂鸦的图片

这一点与`ImageView`没有区别, 调用`setImageUri()`, `setImageResource()`, `setImageBitmap()`等函数即可.  调用这些函数修改涂鸦的图片时会清空当前的所有涂鸦效果.



## Demo

有关上面这些方法的使用, Demo里给出了具体的示例.

待续...



