package com.mulesoft.mql;

import java.util.Collection;

public class Restriction {
    public enum Operator {
        EQUALS, NOT, IN, LIKE, OR, AND, GT, LT, GTE, LTE
    }

    public static final class Property { 
        private String name;

        public Property(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "property[" + name + "]";
        }
        
    }
    
    private Object value;
    private Object left;
    private Operator operator;

    protected Restriction(Operator o, Object left, Object right) {
        this.operator = o;
        this.left = left;
        this.value = right;
    }

    protected Restriction(Operator not, Restriction restriction) {
        this.operator = not;
        this.value = restriction;
    }

    public Object getRight() {
        return value;
    }

    public Object getLeft() {
        return left;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuilder sb) {
        toString(sb, false);
    }

    private void toString(StringBuilder sb, boolean not) {
        switch (operator) {
        case EQUALS:
            sb.append(left);
            if (not) {
                sb.append(" != '");
            } else {
                sb.append(" = '");
            }
            sb.append(value);
            sb.append("'");
            break;
        case LIKE:
            sb.append(left);
            if (not) {
                sb.append(" not");
            }
            sb.append(" like '");
            sb.append(value);
            sb.append("'");
            break;
        case IN:
            sb.append(left);
            sb.append(" in ('");

            boolean first = true;
            for (Object val : ((Collection) value)) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(val).append("'");

            }
            sb.append(")");

            break;
        case OR:
            Restriction or1 = (Restriction) left;
            Restriction or2 = (Restriction) value;
            sb.append("(");
            or1.toString(sb);
            sb.append(" or ");
            or2.toString(sb);
            sb.append(")");

            break;
        case AND:
            Restriction and1 = (Restriction) left;
            Restriction and2 = (Restriction) value;
            sb.append("(");
            and1.toString(sb);
            sb.append(" and ");
            and2.toString(sb);
            sb.append(")");

            break;
        case NOT:
            ((Restriction) value).toString(sb, true);
            break;
        }
    }

    public static Restriction eq(Object left, Object right) {
        return new Restriction(Operator.EQUALS, left, right);
    }
    
    public static Restriction gt(Object left, Object right) {
        return new Restriction(Operator.GT, left, right);
    }
    
    public static Restriction lt(Object left, Object right) {
        return new Restriction(Operator.LT, left, right);
    }
    public static Restriction gte(Object left, Object right) {
        return new Restriction(Operator.GTE, left, right);
    }
    
    public static Restriction lte(Object left, Object right) {
        return new Restriction(Operator.LTE, left, right);
    }

    public static Restriction not(Restriction restriction) {
        return new Restriction(Operator.NOT, restriction);
    }

    public static Restriction like(Object left, Object value) {
        return new Restriction(Operator.LIKE, left, value);
    }

    public static Restriction in(Object left, Collection<?> values) {
        return new Restriction(Operator.IN, left, values);
    }

    public static Restriction or(Restriction left, Restriction right) {
        return new Restriction(Operator.OR, left, right);
    }

    public static Restriction and(Restriction left, Restriction right) {
        return new Restriction(Operator.AND, left, right);
    }
    
    public static Property property(String name) {
        return new Property(name);
    }
}
