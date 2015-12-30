(ns ^:figwheel-always om-tut.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as string]))

(enable-console-print!)

(def app-state
  (atom
   {:people
    [{:type :student :first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
     {:type :student :first "Alyssa" :middle-initial "P" :last "Hacker" :email "aphacker@mit.edu"}
     {:type :professor :first "Gerald" :middle "Jay" :last "Sussman" :email "metacirc@mit.edu" :classes [:6001 :6945]}
     {:type :student :first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
     {:type :student :first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
     {:type :professor :first "Hal" :last "Abelson" :email "evalapply@mit.edu" :classes [:6001]}]
    :classes {:6001 "The Structure and Interpretation of Computer Programs"
              :6945 "The Structure and Interpretation of Classical Mechanics"
              :1806 "Linear Algebra"}}))

;; The tutorial suggests putting this function after app-state. I'm unclear about the reason.
(defn display [show?]
  (if show?
    #js {}
    #js {:display "none"}))

(defn handle-change [e text owner]
  (om/transact! text (fn [_] (.. e -target -value))))

(defn commit-change [text owner]
  (om/set-state! owner :editing false))

;; I must extend the JavaScript string type **before** calling om/root. As the tutorial says, "Putting them near the the top
;; of the file will do nicely.
;;
;; In addition, the following code will produce a **warning** when compiled. The default behavior of figwheel **will not**
;; reload the code. To work around this issue, I'll need to change the project.clj file to allow figwheel to reload code with
;; warnings. 
(extend-type string
  ICloneable
  (-clone [s] (js/String. s))
  ;; To handle the distinction between JavaScript String objects and JavaScript String primitives, I must also extend the
  ;; string type using the om/IValue interface.
  om/IValue
  (-value [s] (str s)))

(defn middle-name [{:keys [middle, middle-initial]}]
  (cond
    middle (str " " middle)
    middle-initial (str " " middle-initial ".")))

(defn display-name [{:keys [first, last] :as contact}]
  (str last ", " first (middle-name contact)))


(defn student-view [student owner]
  (reify
    om/IRender
    (render [_]
      (dom/li nil (display-name student)))))

(defn professor-view [professor owner]
  (reify
    om/IRender
    (render [_]
      (dom/li nil
              (dom/div nil (display-name professor))
              (dom/label nil "Classes")
              (apply dom/li nil
                     (map #(dom/ul nil (om/value %)) (:classes professor)))))))

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

;; Edit a text component.
(defn editable [text owner]
  (reify
    om/IInitState
    (init-state [_]
      ;; Initially, I **am not** editing the component.
      {:editing false})
    om/IRenderState
    (render-state [_ {:keys [editing]}]
      (dom/li nil
              (dom/span #js {:style (display (not editing))} (om/value text))
              (dom/input #js {:style (display editing)
                              ;; Because React **does not** know how to handle JavaScript strings, we must invoke om/value
                              ;; (which requires that we implement om/IValue above).
                              :value (om/value text)
                              :onChange #(handle-change % text owner)
                              :onKeyDown #(when (=  (.-key %) "Enter")
                                            (commit-change text owner))
                              :onBlur (fn [e] (commit-change text owner))
                              }
                         )
              (dom/button #js {:style (display (not editing))
                               :onClick #(om/set-state! owner :editing true)}
                          "Edit")))))

(defn registry-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "registry"}
               (dom/h2 nil "Registry")
               (apply dom/ul nil
                      (om/build-all entry-view (people data)))))))

(defn classes-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "classes"}
               (dom/h2 nil "Classes")
               (apply dom/ul nil
                      (om/build-all editable (vals (:classes data))))))))

(om/root registry-view app-state
         {:target (. js/document (getElementById "registry"))})

(om/root classes-view app-state
         {:target (. js/document (getElementById "classes"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rendering depending on your application
  ;; (swap! app-state update-in [:figwheel-counter] inc)
  )
