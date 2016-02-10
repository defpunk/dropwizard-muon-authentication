(ns muon-dropwizard-authentication.core-test
  (:require [clojure.test :refer :all]
            [muon-dropwizard-authentication.core :refer :all])
  (:import
        [io.dropwizard.auth AuthenticationException]
 	    [io.dropwizard.auth Authenticator]
 	    [io.dropwizard.auth.basic BasicCredentials]
 	    [com.google.common.base Optional]
 	    [java.security Principal]
        [com.qwickr.muon.auth AuthenticatedUser]
        [com.qwickr.muon.auth AuthenticatorFactory]
	)
  )

(defn authenticated-user [n r]
	(AuthenticatedUser. n r))

(def testuser1 (authenticated-user "David" ["Admin" "Test"]))
(def testuser2 (authenticated-user "David" ["Admin" "Driver"]))

(defn- add-then-find-user [k u]
  (add-user k u)
  (find-user k)
  )

(defn- update-then-find-user [k u]
  (update-user k u)
  (find-user k)
  )

(defn- remove-then-find-user [u]
  (remove-user u)
  (find-user u)
  )

(deftest find-users
  (testing "unkown user keys return null"
	(is (= nil (find-user "67867")))
	(is (= nil (find-user "Kelly")))
  ))

(deftest add-users
  (testing "can add and find a user"
    (is (= testuser1 (add-then-find-user "David" testuser1)))
  ))

(deftest add-and-remove-users
  (testing "can add then remove a user"
    (is (= testuser1 (add-then-find-user "David" testuser1)))
    (is (= nil (remove-then-find-user "David")))
   ))

(deftest updating-users
  (testing "can add then remove a user"
    (is (= testuser1 (add-then-find-user "David" testuser1)))
    (is (= testuser2 (update-then-find-user "David" testuser2)))
  ))

(deftest authentication
  (testing "unkown users are not authenticated"
  	(is (= false (.isPresent (authenticate (BasicCredentials. "Fox" "Rabbit")))))
  )
 (testing "known users are authenticated"
  	(is (= true (.isPresent (authenticate (BasicCredentials. "David" "bass")))))
  )
 )

 (deftest build
  (testing "authenticate calls should return optional if AuthenticatedUser"
  	(is (instance? com.qwickr.muon.auth.AuthenticatedUser 
  		  (.get (authenticate (BasicCredentials. "David" "bass"))) ))
   ))

 (deftest event-handling
  (testing "create user event creates a users"
    (is (= {:type "user-created" :user-name "David" :roles ["Admin" "Test"]} (do 
                      (clear-users!)
                      (handle-event {:type "user-created" :user-name "David" :roles ["Admin" "Test"]})
                      (find-user "David")
                      )))
    (is (= {:type "user-created" :user-name "John" :roles ["Admin"]} (do 
                      (clear-users!)
                      (handle-event {:type "user-created" :user-name "John" :roles ["Admin"]})
                      (find-user "John")
                      )))
    )

    (testing "update user event updates a users"
      (is (= {:type "user-updated" :user-name "David" :roles ["Admin" ]} (do 
                        (clear-users!)
                        (handle-event {:type "user-created" :user-name "David" :roles ["Admin" "Test"]})
                        (handle-event {:type "user-updated" :user-name "David" :roles ["Admin"]})
                        (find-user "David")
                        )))
    )

    (testing "user deleted event updates a users"
      (is (= nil (do 
                        (clear-users!)
                        (handle-event {:type "user-created" :user-name "David" :roles ["Admin" "Test"]})
                        (handle-event {:type "user-deleted" :user-name "David" :roles ["Admin"]})
                        (find-user "David")
                        )))
    )
  )

(deftest streamed-event-handling
  (testing "streamed create user event creates a users"
    (is (= {:type "user-created" :user-name "David" :roles ["Admin" "Test"]} (do 
                      (clear-users!)
                      (handle-streamed-event {:payload {:type "user-created" :user-name "David" :roles ["Admin" "Test"]}})
                      (find-user "David")
                      )))
    (is (= {:type "user-created" :user-name "John" :roles ["Admin"]} (do 
                      (clear-users!)
                      (handle-streamed-event {:payload  {:type "user-created" :user-name "John" :roles ["Admin"]}})
                      (find-user "John")
                      )))
    )

    (testing "streamed update user event updates a users"
      (is (= {:type "user-updated" :user-name "David" :roles ["Admin" ]} (do 
                        (clear-users!)
                        (handle-streamed-event {:payload {:type "user-created" :user-name "David" :roles ["Admin" "Test"]}})
                        (handle-streamed-event {:payload  {:type "user-updated" :user-name "David" :roles ["Admin"]}})
                        (find-user "David")
                        )))
    )

    (testing "streamed user deleted event updates a users"
      (is (= nil (do 
                        (clear-users!)
                        (handle-streamed-event {:payload  {:type "user-created" :user-name "David" :roles ["Admin" "Test"]}})
                        (handle-streamed-event {:payload  {:type "user-deleted" :user-name "David" :roles ["Admin"]}})
                        (find-user "David")
                        )))
    )
  )

