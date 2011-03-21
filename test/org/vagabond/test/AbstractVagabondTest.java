package org.vagabond.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.SchemaResolver;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.test.util.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.PropertyWrapper;

public abstract class AbstractVagabondTest {

	@BeforeClass
	public static void setUpLogger () throws FileNotFoundException, IOException, XmlException, ValidationException, SQLException, ClassNotFoundException {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
		QueryHolder.getInstance().loadFromDir(new File ("resource/queries"));
		ConnectionManager.getInstance().getConnection(
				TestOptions.getInstance().getHost(),
				TestOptions.getInstance().getDB(),
				TestOptions.getInstance().getUser(), 
				TestOptions.getInstance().getPassword());
	}
	
	public static void setSchemas (String fileName) throws XmlException, IOException, ValidationException {
		MapScenarioHolder holder;
		
		holder = ModelLoader.getInstance().load(new File(fileName));
		MapScenarioHolder.getInstance().setDocument(holder.getDocument());
		SchemaResolver.getInstance().setSchemas(
				holder.getScenario().getSchemas().getSourceSchema(),
				holder.getScenario().getSchemas().getTargetSchema());
	}
	
}
