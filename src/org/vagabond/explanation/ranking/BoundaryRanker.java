package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerComparators;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType;
import org.vagabond.explanation.ranking.AStarExplanationRanker.RankedListElement;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.util.BitMatrix;
import org.vagabond.util.IdMap;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.ewah.IntIterator;

public class BoundaryRanker implements IExplanationRanker {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			BoundaryRanker.class);
	
	//public void initialize (ExplanationCollection coll);
	//public boolean ready ();
	// user confirmed 
	//public void confirmExplanation (IBasicExplanation correctExpl);
	
	//public IExplanationSet getRankedExpl (int rank);
	//public int getScore (int rank);
	//public int getIterPos();
	//public IExplanationSet previous();
	//public boolean hasPrevious();
	//public int getNumberOfExplSets ();
	//public int getNumberPrefetched ();
	//public void resetIter();
	
	//public boolean hasAtLeast (int numElem); // check that this ranker can produce at least this many explanation sets
	//boolean isFullyRanked();
	//public void rankFull();
	
	private int computeUpBound(int currentUpBound, RankedListElement elem, int BoundCode){
		int retV = 0;
		/*
		 * 1. WeightedCombinedFunc: all weights equal to 1?
		 * 2. Type-Weighted SideEffect Size: 
		 *    current_max + largest weight err-type * additional size of side_effect
		 * 3. Source Error Type Entropy: all new errs add to max-weighted err-type
		 */
		switch (BoundCode){
		    case 1:
		    	
		    	break;
		    	
		    case 2:
		    	elem.max = elem.max + max(errweights) * elem.size;
		    	computeUpBound(elem.max, elem, 2);
		    	break;
		    	
		    case 3:
		    	// new data structure?
		    	// need to store size of err-types and info of current expl-set
				double probability = (ErrTypeCounter.get(ErrType) + new_size) / 
							            ( TotalSize + new_size );
				elem.max = elem.max - probability * Math.log(probability);
		    	break;
		    	
		    default:
		    	
		    	break;
		}				
		return elem.max;
	}
	
	private int computeLowBound(int currentLowBound, RankedListElement elem, int BoundCode){
		/*
		 * 1. WeightedCombinedFunc: all weights equal to 0?
		 * 2. Type-Weighted SideEffect Size: 
		 *    current_min + smallest weight err-type * additional size of side_effect 
		 * 3. Source Error Type Entropy:
		 *    1) increase size of err_type to 5 (or the size of type set)
		 *    2) bin-pack, make the err_type bins equal
		 */
		int retV = 0;
		
		switch (BoundCode){
	    case 1:
	    	
	    	break;
	    	
	    case 2:
	    	elem.min = elem.min + min(errweights) * elem.size;
	    	computeUpBound(elem.min, elem, 2);
	    	break;
	    	
	    case 3:
	    	// new data structure?
	    	// need to store size of err-types and info of current expl-set
	    	int size_diff = sizeof_typeset - sizeof_curr_typeset;
	    	
	    	int init_alloc = min(min(sizeof_curr_typeset), additional_size / size_diff);
	    	
	    	int remain_alloc = additional_size - init_alloc * size_diff;
	    	
	    	if ( remain_alloc >= (size_diff + 1) ){
	    		remain_alloc = remain_alloc - (size_diff + 1);
	    		//increase smallest k+1 bins by 1
	    		
	    	}
	    	else{
	    		//increase smallest bins by 1
	    	}
	    	
	    	//recalculate entropy score
			double retScore = 0.0;
			
			for (ExplanationType ErrType : ErrTypeCounter.keySet())
			{
				double probability = ErrTypeCounter.get(ErrType) / TotalSize;
				retScore = retScore - probability * Math.log(probability);
			}

			elem.min =  retScore;
			
	    	break;
	    	
	    default:
	    	
	    	break;
	}		
		
	    return elem.max;
	
	}
	
	//private int getUpBound(RankedListElement elem){
	//	return elem.max;
	//}
	
	
	private void computeScore (RankedListElement elem) {
		//should elem.min and elem.max set to type double?
		//more scoring functions may not have integer scores
		ArrayList<IBasicExplanation> sets = new ArrayList<IBasicExplanation> ();
		
		elem.min = 0;
		elem.max = 0;
		for(int i = 0; i <  elem.elem.length; i++) {
			if (elem.elem[i] > -1)
				sets.add(errorExpl.get(i).get(elem.elem[i]));
			else if (elem.elem[i] != -2){
				elem.min = Math.max(combinedMin[i], elem.min);
				elem.max += combinedMax[i];
			}
		}
			
		elem.realScore = f.getScore(sets);
		if (elem.isDone()) {
			elem.min = elem.realScore;
			elem.max = elem.realScore;
		}
		else {
			elem.min = Math.max(elem.realScore, elem.min);
			elem.max = elem.realScore + elem.max;
		}
	}
}
