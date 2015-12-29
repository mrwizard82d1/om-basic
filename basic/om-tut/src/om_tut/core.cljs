(ns ^:figwheel-always om-tut.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as string]))

(enable-console-print!)

(def app-state
  (atom
   {:people
    [{:type :student :first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
     {:type :student :first "Alyssa" :middle-initial "P" :last-name "Hacker" :email "aphacker@mit.edu"}
     {:type :professor :first "Gerald" :middle "Jay" :last "Sussman" :email "metacirc@mit.edu" :classes [:6001 :6496]}
     {:type :student :first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
     {:type :student :first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
     {:type :professor :first "Hal" :last "Abelson" :email "evalapply@mit.edu" :classes [:6001]}]
    :classes {:6001 "The Structure and Interpretation of Computer Programs"
              :6945 "The Structure and Interpretation of Classical Mechanics"
              :1806 "Linear Algebra"}}))

(defn middle-name [{:keys [middle, middle-initial]}]
  (cond
    middle (str " " middle)
    middle-initial (str " " middle-initial ".")))

(defn display-name [{:keys [first, last] :as contact}]
  (str last ", " first (middle-name contact)))

(defn student-view [student owner]
  (println "A student:" student)
  (reify
    om/IRender
    (render [_]
      (dom/li nil (display-name student)))))

(defn professor-view [professor owner]
  (println "A professor:" professor)
  (reify
    om/IRender
    (render [_]
      (dom/li nil
              (dom/div nil (display-name professor))
              (dom/label nil "Classes")
              (apply dom/li nil
                     (map #(dom/ul nil %) (:classes professor)))))))

(defmulti entry-view (fn [person _] (:type person)))

(defmethod entry-view :student
  [person owner]
  (student-view person owner))

(defmethod entry-view :professor
  [person owner]
  (professor-view person owner))

(defn people [data]
  (->> data
       :people
       (mapv (fn [x]
               (if (:classes x)
                 (update-in x [:classes]
                            (fn [cs] (mapv (:classes data) cs)))
                 x)))))

(defn registry-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "registry"}
               (dom/h2 nil "Registry")
               (apply dom/ul nil
                      (om/build-all entry-view (people data)))))))

(om/root registry-view app-state
         {:target (. js/document (getElementById "registry"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rendering depending on your application
  ;; (swap! app-state update-in [:figwheel-counter] inc)
  )
