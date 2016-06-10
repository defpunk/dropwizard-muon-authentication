(defproject muon-dropwizard-authentication "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :java-source-paths ["java"]
  :prep-tasks ["javac" "compile"]
  :aot :all
  :dependencies [[org.clojure/clojure "1.8.0"] 
                 [photon-client/photon-client "0.1.1-SNAPSHOT"]
                 [io.dropwizard/dropwizard-auth "0.8.4"] 
                 [org.clojure/tools.logging "0.3.1"]]
)
