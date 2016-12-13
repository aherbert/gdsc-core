package gdsc.core.clustering.optics;

/**
 * Store molecules in a 2D grid and perform distance computation using cells within the radius from the centre. Forces the use of the internal region of the circle.  
 */
class InnerRadialMoleculeSpace extends RadialMoleculeSpace
{
	InnerRadialMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE)
	{
		this(opticsManager, generatingDistanceE, 0);
	}

	InnerRadialMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE, int resolution)
	{
		super(opticsManager, generatingDistanceE, resolution, true);
	}
}