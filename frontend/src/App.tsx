// src/App.tsx
import { Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "./components/ProtectedRoute";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ProfilePage } from "./pages/ProfilePage";
import { AddressesPage } from "./pages/AddressesPage";
import { OrdersPage } from "./pages/OrdersPage";
import { OrderDetailsPage } from "./pages/OrderDetailsPage";
import OrderItemPage from "./pages/OrderItemPage";
import { Layout } from "./components/Layout";

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <OrdersPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/orders/:id"
          element={
            <ProtectedRoute>
              <OrderDetailsPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/orders/:orderId/items/:itemId"
          element={
            <ProtectedRoute>
              <OrderItemPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/addresses"
          element={
            <ProtectedRoute>
              <AddressesPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />

        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Routes>
    </Layout>
  );
}
