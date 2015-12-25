(ns om-tut.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(println "A new developer message. Pay attention, dev guy!")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:list ["Lion" "Zebra" "Buffalo" "Antelope"]}))

(om/root
 (fn [data owner]
   (om/component 
    (apply dom/ul nil
           (map (fn [text] (dom/li nil text)) (:list data)))))
 app-state
 {:target (. js/document (getElementById "app0"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
