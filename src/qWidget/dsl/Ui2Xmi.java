package qWidget.dsl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import qWidget.QHBoxLayout;
import qWidget.QLayout;
import qWidget.QMainWindow;
import qWidget.QPushButton;
import qWidget.QTextEdit;
import qWidget.QVBoxLayout;
import qWidget.QWidgetFactory;
import qWidget.impl.QWidgetFactoryImpl;

public class Ui2Xmi {
	QWidgetFactory factory;

	public Ui2Xmi(QWidgetFactory factory) {
		this.factory = factory;
	}

	public static void main(String[] args) throws SAXException, IOException,
			ParserConfigurationException {
		// parse first args as .ui file path
		String uiFilePath = "QWidgetSampleUI/mainwindow.ui";
		String xmiFilePath = "model/QWidgetInstance.xmi";
		if (args.length > 0) {
			uiFilePath = args[0];
		}
		
		// Initialize the model
		QWidgetFactoryImpl.eINSTANCE.eClass();
		// Retrieve the default factory singleton
		QWidgetFactory factory = QWidgetFactoryImpl.eINSTANCE;

		// create model
		System.out.println("Creating model from " + uiFilePath);
		Ui2Xmi parser = new Ui2Xmi(factory);
		QMainWindow mainWindow = parser.loadModelFromFile(uiFilePath);
//		 QMainWindow mainWindow = parser.loadModelFromCode();

		// Register the XMI resource factory for the .xmi extension
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());

		// create a resource
		ResourceSet resSet = new ResourceSetImpl();
		Resource resource = resSet.createResource(URI
				.createURI(xmiFilePath));
		resource.getContents().add(mainWindow);

		// save content
		System.out.println("Saving model to " + xmiFilePath);
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private QMainWindow loadModelFromCode() {
		QMainWindow mainWindow = factory.createQMainWindow();
		mainWindow.setWindowTitle("MainWindow");
		mainWindow.setHeight(400);
		mainWindow.setWidth(300);

		QVBoxLayout verticalLayout = factory.createQVBoxLayout();
		verticalLayout.setName("verticalLayout");
		mainWindow.setContent(verticalLayout);

		QTextEdit textEdit = factory.createQTextEdit();
		textEdit.setName("textEdit");
		verticalLayout.getContains().add(textEdit);

		QHBoxLayout horizontalLayout = factory.createQHBoxLayout();
		horizontalLayout.setName("horizontalLayout");
		verticalLayout.getContains().add(horizontalLayout);

		QPushButton okButton = factory.createQPushButton();
		okButton.setName("okButton");
		okButton.setText("OK");
		horizontalLayout.getContains().add(okButton);

		QPushButton cancelButton = factory.createQPushButton();
		cancelButton.setName("cancelButton");
		cancelButton.setText("Cancel");
		horizontalLayout.getContains().add(cancelButton);

		return mainWindow;
	}

	private QMainWindow loadModelFromFile(String file) throws SAXException,
			IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(file);
		return parseRoot(dom);
	}

	private QMainWindow parseRoot(Document dom) {
		NodeList widgetElements = dom.getElementsByTagName("widget");
		
		// Main Window
		if(widgetElements.getLength()  != 0) {
			Element mainWidget = (Element) widgetElements.item(0);
			String widgetClass = mainWidget.getAttribute("class");
			if ("QMainWindow".equals(widgetClass)) {
				return parseMainWindow(mainWidget);
			}
		}
		
		return null;
	}

	private QMainWindow parseMainWindow(Element mainWindowElement) {	
		QMainWindow mainWindow = factory.createQMainWindow();
		
		// properties
		List<Element> propertyElements = getChildrenByTagName(
				mainWindowElement, "property");
		for (Element curPropertyElement : propertyElements) {
			String propertyName = curPropertyElement.getAttribute("name");
			switch (propertyName) {
			case "geometry":
				// geometry
				addGeometry(mainWindow, curPropertyElement);
				break;
			case "windowTitle":
				// title
				Element titleElement = getFirstChildByTagName(curPropertyElement, "string");
				if(titleElement != null) {
					String title = parseString(titleElement);
					mainWindow.setWindowTitle(title);
				}
				break;
			default:
				// ignore unknown property
				// TODO add more QMainWindow properties and attributes
				break;
			}
		}

		// content
		Element centralWidgetElement = getFirstChildByTagName(mainWindowElement, "widget");
		if (centralWidgetElement != null) {
			Element centralLayoutElement = getFirstChildByTagName(centralWidgetElement, "layout");
			if(centralLayoutElement != null) {
				QLayout contentLayout = parseLayout(centralLayoutElement);
				if(contentLayout != null) {
					mainWindow.setContent(contentLayout);
				}
			}
		}
		
		return mainWindow;
	}

	private void addGeometry(QMainWindow mainWindow, Element geometryElement) {
		Element rectElement = getFirstChildByTagName(geometryElement, "rect");
		if(rectElement != null) {
			// x
			Element xElement = getFirstChildByTagName(rectElement, "x");
			if(xElement != null) {
				int x = Integer.parseInt(parseString(xElement));
				mainWindow.setX(x);
			}
			// y
			Element yElement = getFirstChildByTagName(rectElement, "y");
			if(yElement != null) {
				int y = Integer.parseInt(parseString(yElement));
				mainWindow.setY(y);
			}
			// width
			Element widthElement = getFirstChildByTagName(rectElement, "width");
			if(widthElement != null) {
				int width = Integer.parseInt(parseString(widthElement));
				mainWindow.setWidth(width);
			}
			// height
			Element heightElement = getFirstChildByTagName(rectElement, "height");
			if(heightElement != null) {
				int height = Integer.parseInt(parseString(heightElement));
				mainWindow.setHeight(height);
			}
		}
	}

	private String parseString(Element stringElement) {
		return stringElement.getTextContent();
	}

	private QLayout parseLayout(Element layoutElement) {
		QLayout layout = null;
		
		// QVBoxLayout or QHBoxLayout ?
		String layoutClass = layoutElement.getAttribute("class");
		if("QVBoxLayout".equals(layoutClass)) {
			layout = factory.createQVBoxLayout();
		} else if ("QHBoxLayout".equals(layoutClass)) {
			layout = factory.createQHBoxLayout();
		} else {
			// ignore unknown layout
			// TODO add more layouts
			return null;
		}
		
		// name
		String layoutName = layoutElement.getAttribute("name");
		layout.setName(layoutName);
		
		// contains
		List<Element> itemElements = getChildrenByTagName(layoutElement, "item");
		for(Element curItem:itemElements) {
			Element innerLayoutElement = getFirstChildByTagName(curItem, "layout");
			Element innerWidgetElement = getFirstChildByTagName(curItem, "widget");
			if(innerLayoutElement != null) {
				// layout
				QLayout innerLayout = parseLayout(innerLayoutElement);
				layout.getContains().add(innerLayout);
			} else if(innerWidgetElement != null) {
				// widget
				String widgetClass = innerWidgetElement.getAttribute("class");
				switch (widgetClass) {
				case "QTextEdit":
					//textedit
					QTextEdit textEdit = parseTextEdit(innerWidgetElement);
					layout.getContains().add(textEdit);
					break;
				case "QPushButton":
					// button
					QPushButton button = parseButton(innerWidgetElement);
					layout.getContains().add(button);
					break;
				default:
					// ignore unknown widget
					// TODO add more widgets
					break;
				}
			}
		}
		return layout;
	}
	
	private QTextEdit parseTextEdit(Element textEditElement) {
		QTextEdit textEdit = factory.createQTextEdit();
		
		// name
		String textEditName = textEditElement.getAttribute("name");
		textEdit.setName(textEditName);
		
		return textEdit;
	}
	
	private QPushButton parseButton(Element buttonElement) {
		QPushButton button = factory.createQPushButton();
		
		// name
		String buttonName = buttonElement.getAttribute("name");
		button.setName(buttonName);
		
		// text
		Element propertyElement = getFirstChildByTagName(buttonElement, "property");
		if(propertyElement != null) {
			String propertyName = propertyElement.getAttribute("name");
			if("text".equals(propertyName)) {
				Element textElement = getFirstChildByTagName(propertyElement, "string");
				if(textElement != null) {
					String text = parseString(textElement);
					button.setText(text);
				}
			}
		}
		
		return button;
	}

	private static List<Element> getChildrenByTagName(Element parent,
			String name) {
		List<Element> nodeList = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& name.equals(child.getNodeName())) {
				nodeList.add((Element) child);
			}
		}
		return nodeList;
	}
	
	private static Element getFirstChildByTagName(Element parent, String name) {
		for (Node child = parent.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& name.equals(child.getNodeName())) {
				return (Element) child;
			}
		}
		return null;
	}

}
