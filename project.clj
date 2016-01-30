(defproject muon-dropwizard-authentication "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :java-source-paths ["java"]
  :prep-tasks ["javac" "compile"]
  :aot :all
  :dependencies [[org.clojure/clojure "1.7.0"] 
                 [io.muoncore/muon-clojure "6.4-20160113114701"] 
                 [io.dropwizard/dropwizard-auth "0.8.4"] 
                 [org.clojure/tools.logging "0.3.1"]]
)
