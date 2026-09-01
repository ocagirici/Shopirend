CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE friendships (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    addressee_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT friendship_not_self CHECK (requester_id <> addressee_id),
    CONSTRAINT friendship_unique_direction UNIQUE (requester_id, addressee_id)
);
CREATE INDEX idx_friendships_addressee ON friendships(addressee_id, status);

CREATE TABLE stores (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

INSERT INTO stores (id, code, name) VALUES
('00000000-0000-0000-0000-000000000001', 'LIDL', 'Lidl'),
('00000000-0000-0000-0000-000000000002', 'KAUFLAND', 'Kaufland'),
('00000000-0000-0000-0000-000000000003', 'TESCO', 'Tesco'),
('00000000-0000-0000-0000-000000000004', 'ALBERT', 'Albert'),
('00000000-0000-0000-0000-000000000005', 'BILLA', 'Billa'),
('00000000-0000-0000-0000-000000000006', 'DM', 'DM'),
('00000000-0000-0000-0000-000000000007', 'OTHER', 'Other');

CREATE TABLE shopping_trips (
    id UUID PRIMARY KEY,
    shopper_id UUID NOT NULL REFERENCES app_users(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);
CREATE INDEX idx_trips_shopper_status ON shopping_trips(shopper_id, status);

CREATE TABLE trip_recipients (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES shopping_trips(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT trip_recipient_unique UNIQUE (trip_id, recipient_id)
);
CREATE INDEX idx_trip_recipients_user ON trip_recipients(recipient_id);

CREATE TABLE trip_requests (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES shopping_trips(id) ON DELETE CASCADE,
    requesting_user_id UUID NOT NULL REFERENCES app_users(id),
    item_name VARCHAR(160) NOT NULL,
    quantity INTEGER NOT NULL,
    note VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT trip_request_quantity_positive CHECK (quantity > 0)
);
CREATE INDEX idx_trip_requests_trip ON trip_requests(trip_id, created_at);

CREATE TABLE shopping_lists (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE shopping_list_items (
    id UUID PRIMARY KEY,
    list_id UUID NOT NULL REFERENCES shopping_lists(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    quantity INTEGER NOT NULL,
    checked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT shopping_list_quantity_positive CHECK (quantity > 0)
);

CREATE TABLE wishlists (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE wishlist_members (
    id UUID PRIMARY KEY,
    wishlist_id UUID NOT NULL REFERENCES wishlists(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT wishlist_member_unique UNIQUE (wishlist_id, member_id)
);

CREATE TABLE wishlist_items (
    id UUID PRIMARY KEY,
    wishlist_id UUID NOT NULL REFERENCES wishlists(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    preferred_store_id UUID REFERENCES stores(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE device_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token VARCHAR(512) NOT NULL UNIQUE,
    platform VARCHAR(30) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_device_tokens_user ON device_tokens(user_id);

