[
  ['analytics', 2.0],
  ['appsforyourdomain', 1.0],
  ['base', 1.0],
  ['blogger', 2.0],
  ['books', 1.0],
  ['calendar', 2.0],
  ['client', 1.0],
  ['codesearch', 2.0],
  ['contacts', 3.0],
  ['core', 1.0],
  ['docs', 3.0],
  ['finance', 2.0],
  ['health', 2.0],
  ['maps', 2.0],
  ['media', 1.0],
  ['photos', 2.0],
  ['spreadsheet', 3.0],
  ['webmastertools', 2.0],
  ['youtube', 2.0]
].each do |pair|
  name, version = *pair
  system "mvn install:install-file -DgroupId=com.google.gdata -DartifactId=gdata-#{name} -Dversion=#{version} -Dfile=/home/flyerhzm/downloads/gdata/java/lib/gdata-#{name}-#{version}.jar -Dpackaging=jar -DgeneratePom=true"
  unless ['base', 'core', 'media'].include? name
    system "mvn install:install-file -DgroupId=com.google.gdata -DartifactId=gdata-#{name}-meta -Dversion=#{version} -Dfile=/home/flyerhzm/downloads/gdata/java/lib/gdata-#{name}-meta-#{version}.jar -Dpackaging=jar -DgeneratePom=true"
  end
end
