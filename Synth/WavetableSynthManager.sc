WavetableSynthManager {
	*new {
		^super.new.init_wavetablesynthmanager;
	}
	
	init_wavetablesynthmanager {
		postln(this.class.asString ++ " initialized");
	}

}