package org.vagabond.explanation.model.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class SourceSkeletonMappingError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	static Logger log = LogProviderHolder.getInstance().getLogger(SourceSkeletonMappingError.class);
	
	private Set<MappingType> mapSE;
	private Set<TransformationType> transSE;
	
	public SourceSkeletonMappingError () {
		super();
		setUp();
	}
	
	public SourceSkeletonMappingError (IAttributeValueMarker marker) {
		super(marker);
		setUp();
	}
	
	public SourceSkeletonMappingError (IAttributeValueMarker marker, 
			Set<MappingType> maps) {
		super(marker);
		setUp();
		this.mapSE = maps;
	}
	
	private void setUp () {
		mapSE = new HashSet<MappingType> ();
		transSE = new HashSet<TransformationType> ();
	}

	@Override
	public ExplanationType getType() {
		return ExplanationType.SourceSkeletonMappingError;
	}

	@Override
	public Object getExplanation() {
		return mapSE;
	}

	@Override
	public int getMappingSideEffectSize() {
		return mapSE.size();
	}
	
	@Override
	public Set<MappingType> getMappingSideEffects() {
		return mapSE;
	}

	public void setMap(Set<MappingType> maps) {
		this.mapSE = maps;
	}

	public void addMap(MappingType map) {
		mapSE.add(map);
	}

	@Override
	public Collection<TransformationType> getTransformationSideEffects () {
		return transSE;
	}
	
	@Override
	public int getTransformationSideEffectSize () {
		return transSE.size();
	}
	
	public void setTransSE (Collection<TransformationType> transSE) {
		this.transSE = new HashSet<TransformationType> (transSE);
	}
}
