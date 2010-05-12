InstrumentBase {
	*new {
		^super.new.init_instrumentbase;
	}
	
	init_instrumentbase {
		// initialize instrument base
		/*
			this class should handle all common functionality between
			WavetableSynth/WavetableSynthFilter
			SampleLooper
			DrumSynth
			GravityGridPlayer
			... and any other top-level instruments
		*/
	}
	
}