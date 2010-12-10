ParamManager : SimpleController {
	var params;
	*new {
		^super.new.init_parammanager;
	}
	
	init_parammanager {
		params = Dictionary();
		postln(this.class.asString ++ " initialized");
	}
	
	add { |par,val|
		// error handling?
		params = params.add(par, val);
	}
}