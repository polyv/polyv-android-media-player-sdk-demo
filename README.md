polyv-android-media-player-sdk-demo
===

[![build passing](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)
[![GitHub release](https://img.shields.io/badge/release-2.1.1-blue.svg)](https://github.com/polyv/polyv-android-media-player-sdk-demo/releases/tag/2.1.1)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [polyv-android-media-player-sdk-demo](#polyv-android-media-player-sdk-demo)
    - [1 简介](#1-%E7%AE%80%E4%BB%8B)
    - [2 体验 Demo](#2-%E4%BD%93%E9%AA%8C-demo)
    - [3 文档](#3-%E6%96%87%E6%A1%A3)
        - [3.1 接口文档](#31-%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3)
        - [3.2 Changelog](#32-changelog)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

### 1 简介
此项目是保利威 Android 播放器 SDK Demo。

此项目只支持基础的播放功能和高级交互功能（包括沉浸式观看、小窗观看），如果您有其它额外的业务功能需求，应根据需要选择对应的 SDK：
1. 如果您需要直播相关的连麦、互动、聊天室等功能，您应当接入 [多场景SDK](https://github.com/polyv/polyv-android-livescenes-sdk-demo)
2. 如果您需要点播相关的下载、上传等功能，您应当接入 [点播SDK](https://github.com/easefun/polyv-android-sdk-2.0-demo)

播放器项目的文件目录结构如下：

```
|-- demo
|   |-- demo
|   |-- mock （demo模拟数据）
|   |-- scene
|   |   |-- feed （短视频场景）
|   |   `-- single （长视频场景）
|   `-- utils （工具类库）
`-- common
    |-- ui （各场景通用的ui组件）
    `-- utils （工具类库）
```

### 2 体验 Demo

Demo [下载链接](https://www.pgyer.com/iE13Ej) （密码：polyv）

### 3 文档
#### 3.1 接口文档
[v2.1.1 接口文档](https://repo.polyv.net/android/documents/media_player_sdk/2.1.1/index.html)
#### 3.2 版本更新记录
[全版本更新记录](./CHANGELOG.md)
