(ns muon-dropwizard-authentication.core
   (:require [muon-clojure.client :as cl] 
            [photon-client.core :as pc] 
            [clojure.core.async :as async :refer [go <! <!!]]
            [clojure.tools.logging :as log] )
 (:import
        [io.dropwizard.auth AuthenticationException]
 	      [io.dropwizard.auth Authenticator]
        [io.muoncore.MultiTransportMuon]
 	      [io.dropwizard.auth.basic BasicCredentials]
 	      [com.google.common.base Optional]
 	      [java.security Principal]
        [com.qwickr.muon.auth AuthenticatedUser]
	)
 )

;The atom that holds our user state
(def ^:private users (atom {}))

 (gen-class
    :main false
    :name com.qwickr.muon.auth.AuthenticatorFactory
     :prefix java-
     :methods [^:static [build [muon_clojure.server.Microservice] io.dropwizard.auth.Authenticator] 
              ])

(defn clear-users! "should only exist for testing" []
  (reset! users {}))

(defn add-user "adds user data with key to stored users" [x m]
  (log/info "Adding user " x)
  (if x (swap! users assoc x m))
)

(defn update-user "adds user data with key to stored users" [x m]
  (log/info "updating user " x)
  (if x (swap! users assoc x m))
)

(defn remove-user "removes user with specified username" [x]
  (log/info "removing user " x)
  (if x (swap! users dissoc x))
)

(defn find-user "returns the user details for the specified key" [x]
  (get @users x) 
)

(defn handle-event [e]
  (let [t (:type e)]
    (log/trace "handle-event:" e)
     (case t
     "user-created" (add-user (:user-name e)  e)
     "user-updated" (update-user (:user-name e) e)
     "user-deleted" (remove-user (:user-name e)) 
     (log/warn "no match for [" t "]")))
)

(defn handle-streamed-event [e] 
  (let [i (pr-str e)]
    (log/trace "handle-streamed-event : " i)
    (handle-event (:payload e))    
  )
)

(defn authenticate "Accepts dropwizard basic credentials and authenticates" [c]
  (log/info "authenticating user" (.getUsername c))
      (if (find-user (.getUsername c))
          (Optional/of (AuthenticatedUser. (.getUsername c) (:roles (find-user (.getUsername c)))))
          (Optional/absent)
        )
      )


(defn authorised-user-subscription [mu]
  (fn [] (cl/with-muon mu (cl/subscribe! "stream://photon/stream"
                        :from 0
                        :stream-type "hot-cold"
                        :stream-name "authorised-user-events")))
  )



;Goes at the end as it depends on the rest
(defn java-build
  "creates a authenticator"
  [mu]
  (log/info "Creating Authenticator")
  (pc/process-hot-channel (authorised-user-subscription mu) (handle-streamed-event))
  (reify io.dropwizard.auth.Authenticator 
    (^Optional authenticate [this c]
      (authenticate c)
    )
  )
 )

