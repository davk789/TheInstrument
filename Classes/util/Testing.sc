TestClass {
	var buffer, server;
	*new {
		^super.new.init_testclass;
	}
	
	init_testclass {
		server = Server.default;
		
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