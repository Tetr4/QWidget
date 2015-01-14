QWidget
=======

QWidget meta model

Loading an instance (\*.xmi) from QWidget UI file (\*.ui):
Run src/qWidget/dsl/Ui2Xmi.java as a java-application. The default .ui file location is *QWidgetSampleUI/mainwindow.ui*. The .xmi file will be generated in *model/QWidgetInstance.xmi*.

Transform QWidget instance to QtQuick instance:
Run Project as ATL Transformation. Select transform/QWidget2QtQuick.atl as ATL Module. Select /QWidget/model/QWidgetInstance.xmi as source model. Select /QtQuick/model/QtQuickInstance.xmi as target model.

