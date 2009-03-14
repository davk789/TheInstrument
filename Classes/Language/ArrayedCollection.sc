+ ArrayedCollection {
	getIndex { |index|
		var ret;
		ret = this.collect({ |obj|
			obj[index];
		});
		^ret;
	}
}

