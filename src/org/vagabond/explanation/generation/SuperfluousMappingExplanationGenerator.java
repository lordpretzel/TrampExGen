package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SuperflousMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;

public class SuperfluousMappingExplanationGenerator 
		implements ISingleExplanationGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(SuperfluousMappingExplanationGenerator.class);
	
	private IAttributeValueMarker error;
	private SuperflousMappingError expl;
	private Set<MappingType> maps;
	private Map<Set<MappingType>,SuperflousMappingError> explsForMap;
	
	public SuperfluousMappingExplanationGenerator () {
		explsForMap = new HashMap<Set<MappingType>,SuperflousMappingError> ();
	}
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker)
			throws Exception {
		IExplanationSet result;
		
		result = ExplanationFactory.newExplanationSet();
		this.error = (IAttributeValueMarker) errorMarker;

		maps = ProvenanceGenerator.getInstance().computeMapProv(error);
		generateExplanation (result);
		
		return result;
	}

	private void generateExplanation (IExplanationSet result) throws Exception {
		Map<String, Set<String>> affRels;
		Set<String> mapSet;
		String relName;

		//Cashing Result
		if (explsForMap.containsKey(maps))
		{
			expl = explsForMap.get(maps);
			result.addExplanation(expl);
		} else {
			expl = new SuperflousMappingError(error);
			affRels = new HashMap<String, Set<String>> ();
					
			for(MappingType map: maps) {
				expl.addMapSE(map);
				
				for(RelAtomType atom: map.getExists().getAtomArray()) {
					relName = atom.getTableref();
					if (!affRels.containsKey(relName)) {
						affRels.put(relName, new HashSet<String> ());
					}
					mapSet = affRels.get(relName);
					mapSet.add(map.getId());
				}
						
				expl.setTransSE(MapScenarioHolder.getInstance().getTransForRels(
						affRels.keySet()));
				
				for (String affRel: affRels.keySet()) {
					computeSideEffects(affRel, affRels.get(affRel));
				}
					
				expl.getTargetSideEffects().remove(error);
				
				result.addExplanation(expl);
				explsForMap.put(maps, expl);
		
			}
		}
	}

	private IMarkerSet computeSideEffects(String rel, Set<String> maps) throws Exception {
		String query;
		ResultSet rs;
		IMarkerSet sideEff = expl.getTargetSideEffects();
		StringBuffer mapList;
		
		mapList = new StringBuffer();
		
		for(String mapName: maps) {
			mapList.append("('" + mapName + "'),");
		}
		mapList.deleteCharAt(mapList.length() - 1);

		query = QueryHolder.getQuery("SuperMap.GetSideEffects")
				.parameterize("target." + rel, mapList.toString());
		if (log.isDebugEnabled()) {log.debug("Run side effect query for <" + rel + "> with query <\n" 
				+ query + ">");};
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		int relId = ScenarioDictionary.getInstance().getRelId(rel);
		int numAtt = ScenarioDictionary.getInstance().getAttrCount(relId);
		while(rs.next())
			for(int attr = 0; attr < numAtt; attr++)
				sideEff.add(MarkerFactory.newAttrMarker(rel, rs.getString(1), attr));
		
		ConnectionManager.getInstance().closeRs(rs);
		
		return sideEff;
	}

}
