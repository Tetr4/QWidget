# QWidget EMF Meta model + QtQuick Transformation
This repository in combination with https://github.com/Tetr4/QtQuick allows transforming [QWidget](https://doc.qt.io/qt-4.8/qwidget.html) UI files to [QtQuick](https://doc.qt.io/qt-4.8/qtquick.html) QML files via [Eclipse Modeling Framework](https://en.wikipedia.org/wiki/Eclipse_Modeling_Framework) meta models.

This repository contains a basic meta model for QWidget. Instances of this model can be created from QWidget UI files (text2model). These instances can then be transformed via [ATL](https://eclipse.org/atl/) to instances of the QtQuick meta model at https://github.com/Tetr4/QtQuick (model2model).

### Loading a model instance (\*.xmi) from QWidget UI file (\*.ui):
Run *src/qWidget/dsl/Ui2Xmi.java* as a java-application. The default .ui file location is *QWidgetSampleUI/mainwindow.ui*. The .xmi file will be generated in *model/QWidgetInstance.xmi*.

### Transform QWidget model instance to QtQuick model instance:
Run Project as ATL Transformation. Select *transform/QWidget2QtQuick.atl* as ATL Module. Select */QWidget/model/QWidgetInstance.xmi* as source model. Choose */QtQuick/model/QtQuickInstance.xmi* as target model.
