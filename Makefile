default: build

build:
	mvn -Dmaven.test.skip=true clean package
	say "finished building heroku examples"
	
