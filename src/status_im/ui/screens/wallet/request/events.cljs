(ns status-im.ui.screens.wallet.request.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.db :as wallet-db]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.chat.constants :as chat-const]
            [status-im.chat.events.input :as input-events]
            [status-im.utils.money :as money]))

(handlers/register-handler-fx
 ::wallet-send-chat-request
 [re-frame/trim-v]
 (fn [{{:contacts/keys [contacts]} :db :as cofx} [asset amount]]
   (handlers-macro/merge-fx cofx
                            {:dispatch [:send-current-message]}
                            (input-events/select-chat-input-command
                             (assoc (get-in contacts chat-const/request-command-ref) :prefill [asset amount]) nil true))))

(handlers/register-handler-fx
 :wallet-send-request
 [re-frame/trim-v]
 (fn [_ [whisper-identity amount symbol decimals]]
   (assert whisper-identity)
   {:dispatch-n [[:navigate-back]
                 [:navigate-to-clean :home]
                 [:add-chat-loaded-event whisper-identity
                  [::wallet-send-chat-request (name symbol) (str (money/internal->formatted amount symbol decimals))]]
                 [:start-chat whisper-identity]]}))

(handlers/register-handler-fx
 :wallet.request/set-recipient
 (fn [{:keys [db]} [_ s]]
   {:db (assoc-in db [:wallet :request-transaction :to] s)}))

(handlers/register-handler-fx
 :wallet.request/set-and-validate-amount
 (fn [{:keys [db]} [_ amount symbol decimals]]
   (let [{:keys [value error]} (wallet-db/parse-amount amount symbol)]
     {:db (-> db
              (assoc-in [:wallet :request-transaction :amount] (money/formatted->internal value symbol decimals))
              (assoc-in [:wallet :request-transaction :amount-text] amount)
              (assoc-in [:wallet :request-transaction :amount-error] error))})))

(handlers/register-handler-fx
 :wallet.request/set-symbol
 (fn [{:keys [db]} [_ symbol]]
   {:db (-> db
            (assoc-in [:wallet :request-transaction :symbol] symbol))}))
