TestClass {
	var buffer, server, privateDataA, privateDataB, parent, bounds, <>action;
	*new { |par,bnd|
		^super.new.init_testclass(par, bnd);
	}
	
	init_testclass { |par,bnd|
		parent = par;
		bounds = bnd;
		privateDataA = "hello";
		privateDataB = "number 2";
		server = Server.default;
/*		action = { |obj| "default action".postln; };
		GUI.button.new(parent, bounds)
		    .states_([["x", Color.red, Color.black]])
		    .action_({ |obj|
				action.value(this);
			});*/
	}

	getMultiArgs { |firstArg ... args|
		postln("here is the firstArg: " ++ firstArg ++ " and now the rest: " ++ args);
	}
	
	getBufferValues {
		1024.do{ |ind|
			buffer.get(ind, { |msg| msg.postln; });
		}
	}
	
	executeSuperclassFunction {
		this.executeSubclassFuction;
	}
	
	/*executeSubclassFuction {
		// "virtual" method
	}*/
}

TestSubClass : TestClass {
	*new {
		^super.new.init_testsubclass;
	}
	
	init_testsubclass {
		postln("initializing " ++ this.class);
	}
	
	executeSubclassFunction {
		postln("inside testSubclass");
	}

}