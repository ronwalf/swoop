package org.mindswap.swoop.utils.owlapi.diff;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEnumeration;
import org.semanticweb.owl.model.change.AddIndividualAxiom;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.AddSuperProperty;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveAnnotationInstance;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDataPropertyInstance;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEnumeration;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.RemoveSuperProperty;
import org.semanticweb.owl.util.OWLConnection;

public class OWLDiffTest extends TestCase {
	//OWLConnection connection;
	OWLOntology src, dst, target;
	
	static URI testURI = URI.create("http://example.com/ontology#");
	
	public static Test suite() {
        return new TestSuite(OWLDiffTest.class);
	}
	
	private OWLOntology createOnt() throws OWLException {
		OWLConnection connection = new OWLConnectionImpl();
		OWLOntology ont = connection.createOntology(testURI, testURI);
		return ont;
	}
	
	private void printChanges(OntologyChange[] changes) throws OWLException {
		
		System.out.println("Changes:");
		for (int i = 0; i < changes.length; ++i) {
			System.out.println(ObjectString.getString(changes[i]));
		}
	}
	
	private OntologyChange[] getChanges(OWLOntology source, OWLOntology destination, OWLOntology target) throws OWLException {
		List changeList = OWLDiff.getChanges(source, destination, target);
		OntologyChange[] changes = new OntologyChange[changeList.size()];
		changes = (OntologyChange[]) changeList.toArray(changes);
		return (OntologyChange[]) changes;
	}
		
	public void setUp() throws Exception {
		src = createOnt();
		dst = createOnt();
		target = createOnt();
	}
	
	public void test_Empty() throws Exception {
		OntologyChange[] changes;
		changes = getChanges(src, dst, target);
		assertEquals(0, changes.length);
	}
	
	public void test_DataPropertyRemoval() throws Exception {
		OntologyChange[] changes;
		OWLEntity entity = src.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop"));
		
		OntologyChange addProperty = new AddEntity(src, entity, null);
		addProperty.accept((ChangeVisitor) src);
		changes = getChanges(src, dst, target);
		assertEquals(1, changes.length);
		assertTrue(changes[0] instanceof RemoveEntity);
	}
	
	public void test_AnnotationRemoval() throws Exception {
		OntologyChange[] changes;
		
		OWLEntity srcEntity = src.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop"));
		OWLAnnotationProperty srcProp = src.getOWLDataFactory().getOWLAnnotationProperty(testURI.resolve("comment"));
		OntologyChange srcChange = new AddAnnotationInstance(src, srcEntity, srcProp, "Hello", null);
		srcChange.accept((ChangeVisitor) src);
		
		
		OWLEntity dstEntity = dst.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop"));
		OWLAnnotationProperty dstProp = dst.getOWLDataFactory().getOWLAnnotationProperty(testURI.resolve("comment"));
		OntologyChange dstChange = new AddEntity(dst, dstEntity, null);
		dstChange.accept((ChangeVisitor) dst);
		dstChange = new AddEntity(dst, dstProp, null);
		dstChange.accept((ChangeVisitor) dst);
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(1, changes.length);
		assertTrue(changes[0] instanceof RemoveAnnotationInstance);
	}
	
	public void test_DataValue() throws Exception {
		OntologyChange[] changes;
		
		OWLIndividual srcIndividual = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("Bob"));
		OWLDataProperty srcProp = src.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop"));
		OWLDataValue srcDV = src.getOWLDataFactory().getOWLConcreteData(null, "en", "Testing");
		OntologyChange srcChange = new AddDataPropertyInstance(src, srcIndividual, srcProp, srcDV, null);
		srcChange.accept((ChangeVisitor) src);
		
		OWLIndividual dstIndividual = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("Bob"));
		OWLDataProperty dstProp = dst.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop"));
		OWLDataValue dstDV = dst.getOWLDataFactory().getOWLConcreteData(null, "en-US", "Testing");
		OntologyChange dstChange = new AddDataPropertyInstance(dst, dstIndividual, dstProp, dstDV, null);
		dstChange.accept((ChangeVisitor) dst);
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(2, changes.length);
		assertTrue(changes[0] instanceof RemoveDataPropertyInstance);
		assertTrue(changes[1] instanceof AddDataPropertyInstance);
		
	}
	
	public void test_ObjectValue() throws Exception {
		OntologyChange[] changes;
		
		OWLIndividual srcBob = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("Bob"));
		OWLObjectProperty srcProp = src.getOWLDataFactory().getOWLObjectProperty(testURI.resolve("knows"));
		OWLIndividual srcRoss = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("Ross"));
		OntologyChange srcChange = new AddObjectPropertyInstance(src, srcBob, srcProp, srcRoss, null);
		srcChange.accept((ChangeVisitor) src);
		
		
		OWLIndividual dstBob = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("Bob"));
		OWLObjectProperty dstProp = dst.getOWLDataFactory().getOWLObjectProperty(testURI.resolve("knows"));
		OWLIndividual dstRoss = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("Ross"));
		OntologyChange dstChange = new AddObjectPropertyInstance(dst, dstRoss, dstProp, dstBob, null);
		dstChange.accept((ChangeVisitor) dst);
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(2, changes.length);
		assertTrue(changes[0] instanceof RemoveObjectPropertyInstance);
		assertTrue(changes[1] instanceof AddObjectPropertyInstance);
	}
	
	public void test_EnumerationRemoval() throws Exception {
		OntologyChange[] changes;
		
		OWLIndividual srcBob = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("Bob"));
		OWLIndividual srcRoss = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("Ross"));
		OWLIndividual srcRobert = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("Robert"));
		Set srcSet1 = new HashSet();
		Set srcSet2 = new HashSet();
		srcSet1.add(srcBob);
		srcSet1.add(srcRoss);
		srcSet2.add(srcBob);
		srcSet2.add(srcRoss);
		srcSet2.add(srcRobert);
		
		OWLEnumeration srcEnum1 = src.getOWLDataFactory().getOWLEnumeration(srcSet1);
		OWLEnumeration srcEnum2 = src.getOWLDataFactory().getOWLEnumeration(srcSet2);
		
		OWLClass srcClass = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class"));
		OntologyChange srcChange1 = new AddEnumeration(src, srcClass, srcEnum1, null);
		srcChange1.accept((ChangeVisitor)src);
		OntologyChange srcChange2 = new AddEnumeration(src, srcClass, srcEnum2, null);
		srcChange2.accept((ChangeVisitor)src);
		
		OWLIndividual dstBob = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("Bob"));
		OWLIndividual dstRoss = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("Ross"));
		OWLIndividual dstRobert = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("Robert"));
		Set dstSet1 = new HashSet();
		
		dstSet1.add(dstBob);
		dstSet1.add(dstRoss);
		dstSet1.add(dstRobert);
		
		OWLEnumeration dstEnum1 = dst.getOWLDataFactory().getOWLEnumeration(dstSet1);
		
		OWLClass dstClass = dst.getOWLDataFactory().getOWLClass(testURI.resolve("Class"));
		OntologyChange dstChange1 = new AddEnumeration(dst, dstClass, dstEnum1, null);
		dstChange1.accept((ChangeVisitor)dst);
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(1, changes.length);
		assertTrue(changes[0] instanceof RemoveEnumeration);
		RemoveEnumeration tgtChange = (RemoveEnumeration) changes[0];
		assertEquals(2, tgtChange.getEnumeration().getIndividuals().size());
	}	
	
	public void test_DisjointRemoval() throws OWLException {
		OntologyChange[] changes;
		
		OWLClass srcClass1 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class1"));
		OWLClass srcClass2 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OWLClass srcClass3 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class3"));
		Set srcDisjoints = new HashSet();
		srcDisjoints.add(srcClass1);
		srcDisjoints.add(srcClass2);
		srcDisjoints.add(srcClass3);
		OWLClassAxiom srcAxiom = src.getOWLDataFactory().getOWLDisjointClassesAxiom(srcDisjoints);
		OntologyChange srcChange = new AddClassAxiom(src, srcAxiom, null);
		srcChange.accept((ChangeVisitor) src);
		
		
		OWLClass dstClass1 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class1"));
		OWLClass dstClass2 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OWLClass dstClass3 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class3"));
		
		Set dstClasses = new HashSet();
		dstClasses.add(dstClass1);
		dstClasses.add(dstClass2);
		dstClasses.add(dstClass3);
		for (Iterator iter = dstClasses.iterator(); iter.hasNext(); ) {
			OntologyChange dstChange = new AddEntity(dst, (OWLEntity) iter.next(), null);
			dstChange.accept((ChangeVisitor) dst);
		}
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(1, changes.length);
		assertTrue(changes[0] instanceof RemoveClassAxiom);
		RemoveClassAxiom tgtChange = (RemoveClassAxiom) changes[0];
		assertTrue(tgtChange.getAxiom() instanceof OWLDisjointClassesAxiom);
	}
	
	public void test_DifferentIndividuals() throws OWLException {
		OntologyChange[] changes;
		
		OWLIndividual srcBob = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("bob"));
		OWLIndividual srcJon = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("jon"));
		OWLIndividual srcJoe = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("joe"));
		Set srcDifferents = new HashSet();
		srcDifferents.addAll(Arrays.asList(new OWLIndividual[] {srcBob, srcJon, srcJoe}));
		OWLDifferentIndividualsAxiom srcDifferent = src.getOWLDataFactory().getOWLDifferentIndividualsAxiom(srcDifferents);
		OntologyChange srcChange = new AddIndividualAxiom(src, srcDifferent, null);
		srcChange.accept((ChangeVisitor) src);
		
		OWLIndividual dstBob = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("bob"));
        OWLIndividual dstJon = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("jon"));
        OWLIndividual dstJoe = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("joe"));
        Set dstDifferents = new HashSet();
        dstDifferents.addAll(Arrays.asList(new OWLIndividual[] {dstBob, dstJon, dstJoe}));
        OWLDifferentIndividualsAxiom dstDifferent = dst.getOWLDataFactory().getOWLDifferentIndividualsAxiom(dstDifferents);
        OntologyChange dstChange = new AddIndividualAxiom(dst, dstDifferent, null);
        dstChange.accept((ChangeVisitor) dst);

		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(0, changes.length);
	}
	
	public void test_Types() throws OWLException {
		OntologyChange[] changes;
		
		OWLIndividual srcBob = src.getOWLDataFactory().getOWLIndividual(testURI.resolve("bob"));
		OWLClass srcClass = src.getOWLDataFactory().getOWLClass(testURI.resolve("TestClass"));
		OntologyChange srcChange = new AddIndividualClass(src, srcBob, srcClass, null);
		srcChange.accept((ChangeVisitor) src);
		
		OWLIndividual dstBob = dst.getOWLDataFactory().getOWLIndividual(testURI.resolve("bob"));
		OWLClass dstClass = dst.getOWLDataFactory().getOWLClass(testURI.resolve("TestClass"));
		OntologyChange dstChange1 = new AddEntity(dst, dstBob, null);
		dstChange1.accept((ChangeVisitor) dst);
		OntologyChange dstChange2 = new AddEntity(dst, dstClass, null);
		dstChange2.accept((ChangeVisitor) dst);
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(1, changes.length);
		assertTrue(changes[0] instanceof RemoveIndividualClass);
	}
	
	public void test_ClassRemoval() throws OWLException {
		OntologyChange[] changes;
		
		OWLClass srcClass1 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class1"));
		OWLClass srcClass2 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OWLClassAxiom srcAxiom = src.getOWLDataFactory().getOWLSubClassAxiom(srcClass1, srcClass2);
		OntologyChange srcChange = new AddClassAxiom(src, srcAxiom, null);
		srcChange.accept((ChangeVisitor)src);
		
		OWLClass dstClass2 = dst.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OntologyChange dstChange = new AddEntity(dst, dstClass2, null);
		dstChange.accept((ChangeVisitor)dst);
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(2, changes.length);
		assertTrue(changes[0] instanceof RemoveEntity);
		assertTrue(changes[1] instanceof RemoveClassAxiom);
		
	}
	
	public void test_ClassAddition() throws OWLException {
		OntologyChange[] changes;
		
		OWLClass srcClass1 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class1"));
		OWLClass srcClass2 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OWLClassAxiom srcAxiom = src.getOWLDataFactory().getOWLSubClassAxiom(srcClass1, srcClass2);
		OntologyChange srcChange = new AddClassAxiom(src, srcAxiom, null);
		srcChange.accept((ChangeVisitor)src);
		
		OWLClass dstClass2 = dst.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OntologyChange dstChange = new AddEntity(dst, dstClass2, null);
		dstChange.accept((ChangeVisitor)dst);
		
		changes = getChanges(dst, src, target);
		System.out.print("ClassAddition:");
		printChanges(changes);
		assertEquals(2, changes.length);
		assertTrue(changes[0] instanceof AddEntity);
		assertTrue(changes[1] instanceof AddClassAxiom);
		
	}
	
	public void test_ClassNameChange() throws OWLException {
		OntologyChange[] changes;
		
		OWLClass srcClass1 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class1a"));
		OWLClass srcClass2 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OWLClassAxiom srcAxiom = src.getOWLDataFactory().getOWLSubClassAxiom(srcClass1, srcClass2);
		OntologyChange srcChange = new AddClassAxiom(src, srcAxiom, null);
		srcChange.accept((ChangeVisitor)src);
		
		OWLClass dstClass1 = dst.getOWLDataFactory().getOWLClass(testURI.resolve("Class1b"));
		OWLClass dstClass2 = dst.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OWLClassAxiom dstAxiom = dst.getOWLDataFactory().getOWLSubClassAxiom(dstClass1, dstClass2);
		OntologyChange dstChange = new AddClassAxiom(dst, dstAxiom, null);
		dstChange.accept((ChangeVisitor)dst);
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(4, changes.length);
		assertTrue(changes[0] instanceof RemoveEntity);
		assertTrue(changes[1] instanceof RemoveClassAxiom);
		assertTrue(changes[2] instanceof AddEntity);
		assertTrue(changes[3] instanceof AddClassAxiom);
	}
	
	public void test_SuperClassRemoval() throws OWLException {
		OntologyChange[] changes;
		
		OWLClass srcClass1 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class1"));
		OWLClass srcClass2 = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OntologyChange srcChange = new AddSuperClass(src, srcClass1, srcClass2, null);
		srcChange.accept((ChangeVisitor)src);
		
		OWLClass dstClass2 = dst.getOWLDataFactory().getOWLClass(testURI.resolve("Class2"));
		OntologyChange dstChange = new AddEntity(dst, dstClass2, null);
		dstChange.accept((ChangeVisitor)dst);
		
		changes = getChanges(src, dst, target);
		printChanges(changes);
		assertEquals(2, changes.length);
		assertTrue(changes[0] instanceof RemoveEntity);
		assertTrue(changes[1] instanceof RemoveSuperClass);
	}
	
	public void test_DomainRemoval() throws OWLException {
		OntologyChange[] changes;
		OWLDataProperty srcProp = src.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop"));
		OWLClass srcClass = src.getOWLDataFactory().getOWLClass(testURI.resolve("Class"));
		OntologyChange srcChange = new AddDomain(src, srcProp, srcClass, null);
		srcChange.accept((ChangeVisitor) src);
		
		
		OWLDataProperty dstProp = dst.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop"));
		OntologyChange dstChange = new AddEntity(dst, dstProp, null);
		dstChange.accept((ChangeVisitor)dst);
		
		OWLClass dstClass = dst.getOWLDataFactory().getOWLClass(testURI.resolve("Class"));
		dstChange = new AddEntity(dst, dstClass, null);
		dstChange.accept((ChangeVisitor)dst);
		
		changes = getChanges(src, dst, target);
		assertEquals(1, changes.length);
		assertTrue(changes[0] instanceof RemoveDomain);
	}
	
	public void test_SubPropRemoval() throws OWLException {
		OntologyChange[] changes;
		
		OWLDataProperty srcProp1 = src.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop1"));
		OWLDataProperty srcProp2 = src.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop2"));
		OntologyChange srcChange = new AddSuperProperty(src, srcProp1, srcProp2, null);
		srcChange.accept((ChangeVisitor)src);
		
		OWLDataProperty dstProp1 = dst.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop1"));
		OntologyChange dstChange = new AddEntity(dst, dstProp1, null);
		dstChange.accept((ChangeVisitor)dst);
		OWLDataProperty dstProp2 = dst.getOWLDataFactory().getOWLDataProperty(testURI.resolve("prop2"));
		dstChange = new AddEntity(dst, dstProp2, null);
		dstChange.accept((ChangeVisitor)dst);
		
		changes = getChanges(src, dst, target);
		assertEquals(1, changes.length);
		assertTrue(changes[0] instanceof RemoveSuperProperty);
	}
}
