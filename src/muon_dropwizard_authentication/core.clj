(ns muon-dropwizard-authentication.core
   (:require [muon-clojure.client :as cl] 
            [clojure.core.async :as async :refer [go <! <!!]]
            [clojure.tools.logging :as log] )
 (:import
        [io.dropwizard.auth AuthenticationException]
 	      [io.dropwizard.auth Authenticator]
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
     :methods [^:static [build [] io.dropwizard.auth.Authenticator] 
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

;(defn subscribe [mu]
;  (let [ch (cl/with-muon mu (cl/subscribe! "stream://photon/stream"
;                        :from 0
;                        :stream-type "hot-cold"
;                        :stream-name "authorised-user-events"))]
;  (go (loop [elem (<!! ch)] (handle-streamed-event elem) (recur (<!! ch))))))

(defn authenticate "Accepts dropwizard basic credentials and authenticates" [c]
  (log/info "authenticating user" (.getUsername c))
      (if (find-user (.getUsername c))
          (Optional/of (AuthenticatedUser. (.getUsername c) (:roles (find-user (.getUsername c)))))
          (Optional/absent)
        )
      )

(defn process-hot-channel 
  "Expects two functions a no arg to create the channel and a single arg to handle events placed on the channel" 
  [cf hf]
  (let [ch (cf)]
    (go (loop [elem (<!! ch)] (hf elem) (recur (<!! ch))))))


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
  (log/info "creating Authenticator")
  (process-hot-channel (authorised-user-subscription mu) (handle-streamed-event))
  (reify io.dropwizard.auth.Authenticator 
    (^Optional authenticate [this c]
      (authenticate c)
    )
  )
 )

