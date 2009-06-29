TestClass {
	var buffer, server;
	*new {
		^super.new.init_testclass;
	}
	
	init_testclass {
		server = Server.default;
		buffer = Buffer.read(server, "/Users/davk/Music/SuperCollider_Samples/SC_090419_115207/1.aif");
	}
	
	getBufferValues {
		1024.do{ |ind|
			buffer.get(ind, { |msg| msg.postln; });
		}
	}
}